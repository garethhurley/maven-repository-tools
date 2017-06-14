package com.simpligility.maven.provisioner;



import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;

public class PackagingCheckerTest 
{
    
    private Artifact createTestAritfact( String artifactCoordinate ) 
    {
         List<Artifact> artifacts = new ArrayList<Artifact>();
         Artifact artifact = new DefaultArtifact( artifactCoordinate );
         artifacts.add( artifact );
         return artifact;
    }
    

    @Test
    public void testToAssertASecondArtifactIsCreated() 
    {
         String artifactCoordinate = "aopalliance:aopalliance:jar:1.0";
         Artifact artifact = createTestAritfact( artifactCoordinate );
         List<Artifact> processedArtifacts = 
               PackagingChecker.postProcess( artifactCoordinate, artifact );         

         PackagingChecker.postProcess( artifactCoordinate, artifact );
         assertEquals( processedArtifacts.size(), 2 );
    }
    
    
    @Test
    public void testToAssertJarCoordinatesGeneratePomCoordinates() 
    {
         String artifactCoordinate = "aopalliance:aopalliance:jar:1.0";
         Artifact artifact = createTestAritfact( artifactCoordinate );
         List<Artifact> processedArtifacts = 
              PackagingChecker.postProcess( artifactCoordinate, artifact );                  
         Artifact newArtifact = processedArtifacts.get( 1 );
         assertEquals( newArtifact.getExtension(), "pom" ); 
    }
    
    
    
    @Test
    public void testToAssertPomCoordinatesGenerateJarCoordinates() 
    {
         String artifactCoordinate = "aopalliance:aopalliance:pom:1.0";
         Artifact artifact = createTestAritfact( artifactCoordinate );
         List<Artifact> processedArtifacts = PackagingChecker.postProcess( artifactCoordinate, artifact );
         assertEquals( processedArtifacts.size(), 1 ); 
    }



    
    @Test
    public void testToAssertNoPackagingSpecificatioResultsInBothJarAndPom() 
    {
        String artifactCoordinate = "aopalliance:aopalliance:1.0";
        Artifact artifact = createTestAritfact( artifactCoordinate );
        List<Artifact> processedArtifacts = PackagingChecker.postProcess( artifactCoordinate, artifact );
         
        assertEquals( processedArtifacts.size(), 2 ); 
        assertEquals( processedArtifacts.get( 0 ).getExtension(), "jar" ); 
        assertEquals( processedArtifacts.get( 1 ).getExtension(), "pom" ); 
    }


}
