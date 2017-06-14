package com.simpligility.maven.provisioner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

public class PackagingChecker 
     {

    private static final String POM = "pom";

    private static final String JAR = "jar";


    public static List<Artifact> postProcess( String artifactCoordinate, Artifact artifact )
    { 
        List<Artifact> artifacts = new ArrayList<Artifact>();
        artifacts.add( artifact );
        String extension =  artifact.getExtension();
        if ( extension.equals( JAR ) && artifactCoordinate.contains( JAR ) ) 
        {
            String artifactCoordinateCopy = artifactCoordinate.replaceAll(
                   String.format( ":%s:", JAR ) , String.format( ":%s:", POM ) );
            artifacts.add( new DefaultArtifact( artifactCoordinateCopy ) );
         }
        else if ( extension.equals( POM ) )
        {
            return artifacts;
        }
        else
        {
        String artifactCoordinateCopy = artifactCoordinate.replaceAll(
               artifact.getVersion(),  String.format( "%s:%s", POM, artifact.getVersion() ) );
        artifacts.add( new DefaultArtifact( artifactCoordinateCopy ) );
        }
        return artifacts;
    }

}
