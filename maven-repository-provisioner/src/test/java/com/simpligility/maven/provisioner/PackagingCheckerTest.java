package com.simpligility.maven.provisioner;



import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.junit.Test;

public class PackagingCheckerTest 
{
    
    private List<Artifact> postProcessArtifact( String inputArtifactCoordinate )
    {
         return PackagingChecker.postProcess( inputArtifactCoordinate );
    }

    @Test
    public void testToAssertJarCoordinatesGeneratePomCoordinates() 
    {
         assertActualArtifactListMatches( postProcessArtifact( "aopalliance:aopalliance:jar:1.0" ),
                 "aopalliance:aopalliance:jar:1.0", "aopalliance:aopalliance:pom:1.0" );
    }

    @Test
    public void generatedPomForClassifierJarDoesNotIncludeClassifier()
    {
        assertActualArtifactListMatches( postProcessArtifact( "aopalliance:aopalliance:jar:myclassifier:1.0" ),
                "aopalliance:aopalliance:jar:myclassifier:1.0", "aopalliance:aopalliance:pom:1.0" );
    }
    
    @Test
    public void testToAssertPomCoordinatesGenerateJarCoordinates() 
    {
         assertActualArtifactListMatches( postProcessArtifact( "aopalliance:aopalliance:pom:1.0" ),
                 "aopalliance:aopalliance:pom:1.0" );
    }

    @Test
    public void testToAssertNoPackagingSpecificationResultsInBothJarAndPom()
    {
        assertActualArtifactListMatches( postProcessArtifact( "aopalliance:aopalliance:1.0" ),
                "aopalliance:aopalliance:jar:1.0", "aopalliance:aopalliance:pom:1.0" );
    }

    private void assertActualArtifactListMatches( List<Artifact> actualArtifactList, String... expectedArtifact )
    {
        assertEquals( "actual generated list of artifacts has unexpected size",
                expectedArtifact.length, actualArtifactList.size() );
        for ( int i = 0; i < actualArtifactList.size(); i++ )
        {
            assertEquals( expectedArtifact[i] , actualArtifactList.get( i ).toString() );
        }
    }

}
