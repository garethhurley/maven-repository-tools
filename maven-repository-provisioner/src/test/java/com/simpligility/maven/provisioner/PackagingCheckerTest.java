package com.simpligility.maven.provisioner;



import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;

public class PackagingCheckerTest 
{
    
    private List<Artifact> createTestAritfact( String artifactCoordinate ) 
    {
         return PackagingChecker.postProcess( artifactCoordinate, new DefaultArtifact( artifactCoordinate  ) 
                  );
    }
    

    @Test
    public void testToAssertASecondArtifactIsCreated() 
    {
         List<Artifact> processedArtifacts = createTestAritfact( "aopalliance:aopalliance:jar:1.0" ); 
         assertEquals( processedArtifacts.size(), 2 );
    }
    
    
    @Test
    public void testToAssertJarCoordinatesGeneratePomCoordinates() 
    {
         List<Artifact> processedArtifacts =  createTestAritfact( "aopalliance:aopalliance:jar:1.0" );                  
         Artifact newArtifact = processedArtifacts.get( 1 );
         assertEquals( newArtifact.getExtension(), "pom" ); 
    }
    
    
    
    @Test
    public void testToAssertPomCoordinatesGenerateJarCoordinates() 
    {
         List<Artifact> processedArtifacts = createTestAritfact( "aopalliance:aopalliance:pom:1.0" );
         assertEquals( processedArtifacts.size(), 1 ); 
    }



    
    @Test
    public void testToAssertNoPackagingSpecificatioResultsInBothJarAndPom() 
    {
        List<Artifact> processedArtifacts = createTestAritfact(  "aopalliance:aopalliance:1.0" );
         
        assertEquals( processedArtifacts.size(), 2 ); 
        assertEquals( processedArtifacts.get( 0 ).getExtension(), "jar" ); 
        assertEquals( processedArtifacts.get( 1 ).getExtension(), "pom" ); 
    }


}
