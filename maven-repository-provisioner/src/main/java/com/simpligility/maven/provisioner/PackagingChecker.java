package com.simpligility.maven.provisioner;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

public class PackagingChecker 
     {

    private static final String POM = "pom";

    private static final String JAR = "jar";


    public static void filter( 
           List<Artifact> artifacts, String artifactCoordinate, Artifact artifact, String extension ) 
    {
        if ( extension.equals( JAR ) && artifactCoordinate.contains( JAR ) ) 
        {
            String artifactCoordinateCopy = artifactCoordinate.replaceAll(
                   String.format( ":%s:", JAR ) , String.format( ":%s:", POM ) );
            artifacts.add( new DefaultArtifact( artifactCoordinateCopy ) );
         }
        else if ( extension.equals( POM ) )
        {
            return;
        }
        else
        {
        String artifactCoordinateCopy = artifactCoordinate.replaceAll(
               artifact.getVersion(),  String.format( "%s:%s", POM, artifact.getVersion() ) );
        artifacts.add( new DefaultArtifact( artifactCoordinateCopy ) );
        }
    }

}
