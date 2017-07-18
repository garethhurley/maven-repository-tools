/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.simpligility.maven.Gav;
import com.simpligility.maven.MavenConstants;

/**
 * ArtifactRetriever can resolve a dependencies and all transitive dependencies and upstream parent pom's 
 * for a given GAV coordinate and fill a directory with the respective Maven repository containing those components.
 * 
 * @author Manfred Moser - manfred@simpligility.com
 */
public class ArtifactRetriever
{

    private static final String POM = "pom";

    private static Logger logger = LoggerFactory.getLogger( "ArtifactRetriever" );

    private RepositorySystem system;

    private DefaultRepositorySystemSession session;

    private File repositoryPath;

    private RemoteRepository sourceRepository;

    private final TreeSet<String> successfulRetrievals = new TreeSet<String>();

     private final TreeSet<String> failedRetrievals = new TreeSet<String>();
     
     private final List<RemoteRepository> remoteRepositoryList = new ArrayList<RemoteRepository>();


    public ArtifactRetriever( File repositoryPath )
    {
        this.repositoryPath = repositoryPath;
        initialize();
    }

    private void initialize()
   {
        system = RepositoryHandler.getRepositorySystem();
        session = RepositoryHandler.getRepositorySystemSession( system, repositoryPath );
    }

    public void retrieve( List<String> artifactCoordinates, String sourceUrl, boolean includeSources,
           boolean includeJavadoc, boolean includeProvidedScope, boolean includeTestScope )
    {
        RemoteRepository.Builder builder = new RemoteRepository.Builder( "central", "default", sourceUrl );
        builder.setProxy( ProxyHelper.getProxy( sourceUrl ) );
        sourceRepository = builder.build();
        remoteRepositoryList.add( sourceRepository );

        getArtifactResults( artifactCoordinates, includeProvidedScope, includeTestScope );

        getAdditionalArtifacts( includeSources, includeJavadoc );
    }

     private List<ArtifactResult> getArtifactResults( List<String> artifactCoordinates, boolean includeProvidedScope,
            boolean includeTestScope )
    {

        List<Artifact> artifacts = new ArrayList<Artifact>();
        

        for ( String artifactCoordinate : artifactCoordinates )
        {
            artifacts.addAll( PackagingChecker.postProcess( artifactCoordinate ) );
        }

        List<ArtifactResult> artifactResults = new ArrayList<ArtifactResult>();

        Collection<String> includes = new ArrayList<String>();
        // we always include compile scope, not doing that makes no sense
        includes.add( JavaScopes.COMPILE );

        Collection<String> excludes = new ArrayList<String>();
        // always exclude system scope since it is machine specific and wont work in 99% of cases
        excludes.add( JavaScopes.SYSTEM );

        if ( includeProvidedScope )
        {
            includes.add( JavaScopes.PROVIDED );
        }
        else 
        {
            excludes.add( JavaScopes.PROVIDED ); 
        }

        if ( includeTestScope ) 
        {
            includes.add( JavaScopes.TEST );
        }
        else
        {
            excludes.add( JavaScopes.TEST );
        }

        DependencySelector selector =
                new AndDependencySelector(
                        new ScopeDependencySelector( includes, excludes ),
                        new OptionalDependencySelector(),
                        new ExclusionDependencySelector()
                        );
        session.setDependencySelector( selector );

        for ( Artifact artifact : artifacts )
        {
            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
            collectRequest.addRepository( sourceRepository );

            ArtifactRequest request = new ArtifactRequest( artifact, remoteRepositoryList, null );

            try
            {
                ArtifactResult resolvedDependencies = system.resolveArtifact( session, request );
                artifactResults.add( resolvedDependencies );
                successfulRetrievals.add( resolvedDependencies.getArtifact().toString() );
            }
            catch ( NullPointerException | ArtifactResolutionException npe )
            {
                logger.info( "NullPointerException resolving dependencies ", npe );
                failedRetrievals.add( npe.getMessage() );
            }
        }

        return artifactResults;
    }


    private void getAdditionalArtifacts( boolean includeSources, boolean includeJavadoc )
    {
        Collection<File> pomFiles = MavenRepositoryDeployer.getPomFiles( repositoryPath );
        for ( File pomFile : pomFiles )
        {
            logger.info( "Processing POM file " + pomFile.getAbsolutePath() );
            Gav gav = null;
            try
            {
                gav = MavenRepositoryDeployer.getCoordinates( pomFile );
            }
            catch ( Exception e )
            {
                logger.info( "Failed to retrieve gav from " + pomFile.getAbsolutePath() );
            }

            if ( !POM.equals( gav.getPackaging() ) )
            {
                // this gets e.g. a .hpi file in addition to a .jar
                // but also causes failed retrievals for files where the packaging is NOT used for extension
                // an example is bundle packaging, there is no .bundle file, just  the .jar
                // these failures are false warnings since at this stage the main artifact as jar is already retrieved
                getMainArtifact( gav );
                if ( includeSources )
                {
                    getSourcesJar( gav );
                }
                if ( includeJavadoc )
                {
                    getJavadocJar( gav );
                }
            }
        }
    }

    private void getMainArtifact( Gav gav )
    {
        getArtifact( gav, null, null );
    }

    private void getSourcesJar( Gav gav )
    {
        getArtifact( gav, MavenConstants.JAR, MavenConstants.SOURCES );
    }

    private void getJavadocJar( Gav gav )
    {
        getArtifact( gav, MavenConstants.JAR, MavenConstants.JAVADOC );
    }

    private void getArtifact( Gav gav, String packagingOverride, String classifier )
    {
        String packaging;
        if ( StringUtils.isNotEmpty( packagingOverride ) )
        {
            packaging = packagingOverride;
        }
        else
        {
            packaging = gav.getPackaging();
        }
        Artifact artifact = new DefaultArtifact( gav.getGroupId(), gav.getArtifactId(), classifier, packaging,
                gav.getVersion() );
        // avoid download if we got it locally already? or not bother and just get it again? 
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact( artifact );
        artifactRequest.addRepository( sourceRepository );

        try
        {
            ArtifactResult artifactResult = system.resolveArtifact( session, artifactRequest );
            logger.info( "Retrieved " + artifactResult.getArtifact().getFile() );

            successfulRetrievals.add( artifact.toString() );
        }
        catch ( ArtifactResolutionException e )
        {
            logger.info( "ArtifactResolutionException when retrieving " + classifier );
            failedRetrievals.add( e.getMessage() );
        }
    }

    public String listSucessfulRetrievals()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Sucessful Retrievals:\n\n" );
        for ( String artifact : successfulRetrievals ) 
        {
            builder.append( artifact + "\n" );
        }
        return builder.toString();
    }

    public String listFailedTransfers()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Failed Retrievals:\n\n" );
        for ( String artifact : failedRetrievals ) 
        {
            builder.append( artifact + "\n" );
        }
        return builder.toString();
    }

    public boolean hasFailures() 
    {
        return failedRetrievals.size() > 0;
    }

    public String getFailureMessage() 
    {
        return "Failed to retrieve some artifacts.";
    }

}
