package com.simpligility.maven.provisioner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

public class PackagingChecker 
     {

    private static final String POM = "pom";

    private static final String JAR = "jar";


    public static List<Artifact> postProcess( String artifactCoordinate )
    { 
        List<Artifact> artifacts = new ArrayList<Artifact>();
        Artifact artifact = new DefaultArtifact( artifactCoordinate );
        artifacts.add( artifact );
        String extension =  artifact.getExtension();
        if ( extension.equals( JAR ) )
        {
            if ( hasNoClassifier( artifact ) )
            {
                artifacts.add( new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                        POM, artifact.getVersion() ) );
            }
            else
            {
                artifacts.add( new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                        POM, artifact.getVersion() ) );
            }
        }
        else if ( extension.equals( POM ) )
        {
            return artifacts;
        }
        return artifacts;
    }

         private static boolean hasNoClassifier( Artifact artifact )
         {
             return "".equals( artifact.getClassifier() );
         }

     }
