package com.simpligility.maven.provisioner;



import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;

public class PackagingCheckerTest 
{

    @Test
    public void testToAssertASecondArtifactIsCreated() 
    {
         List<Artifact> artifacts = new ArrayList<Artifact>();
         String artifactCoordinate = "aopalliance:aopalliance:jar:1.0";
         Artifact artifact = new DefaultArtifact( artifactCoordinate );
         artifacts.add( artifact );
         String extension = artifact.getExtension();

         assertEquals( artifacts.size(), 1 );
         PackagingChecker.filter( artifacts, artifactCoordinate, artifact, extension );
         assertEquals( artifacts.size(), 2 );
    }
    
    
    @Test
    public void testToAssertJarCoordinatesGeneratePomCoordinates() 
    {
         List<Artifact> artifacts = new ArrayList<Artifact>();
         String artifactCoordinate = "aopalliance:aopalliance:jar:1.0";
         Artifact artifact = new DefaultArtifact( artifactCoordinate );
         artifacts.add( artifact );
         String extension = artifact.getExtension();

         PackagingChecker.filter( artifacts, artifactCoordinate, artifact, extension );
         
         Artifact newArtifact = artifacts.get( 1 );
         assertEquals( newArtifact.getExtension(), "pom" ); 
    }
    
    
    
    @Test
    public void testToAssertPomCoordinatesGenerateJarCoordinates() 
    {
         List<Artifact> artifacts = new ArrayList<Artifact>();
         String artifactCoordinate = "aopalliance:aopalliance:pom:1.0";
         Artifact artifact = new DefaultArtifact( artifactCoordinate );
         artifacts.add( artifact );
         String extension = artifact.getExtension();

         PackagingChecker.filter( artifacts, artifactCoordinate, artifact, extension );
         
         assertEquals( artifacts.size(), 1 ); 
    }
    
    
    @Test
    public void testToAssertNoPackagingSpecificatioResultsInBothJarAndPom() 
    {
         List<Artifact> artifacts = new ArrayList<Artifact>();
         String artifactCoordinate = "aopalliance:aopalliance:1.0";
         Artifact artifact = new DefaultArtifact( artifactCoordinate );
         artifacts.add( artifact );
         String extension = artifact.getExtension();

         PackagingChecker.filter( artifacts, artifactCoordinate, artifact, extension );
         
         assertEquals( artifacts.size(), 2 ); 
         assertEquals( artifacts.get( 0 ).getExtension(), "jar" ); 
         assertEquals( artifacts.get( 1 ).getExtension(), "pom" ); 
    }


}
