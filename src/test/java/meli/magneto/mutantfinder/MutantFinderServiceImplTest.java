package meli.magneto.mutantfinder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.TestCase.assertTrue;
import static meli.magneto.mutantfinder.Statistics.HUMAN_DNA;
import static meli.magneto.mutantfinder.Statistics.MUTANT_DNA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class MutantFinderServiceImplTest {

    @InjectMocks
    private MutantFinderServiceImpl fixture;

    @Mock
    private StatisticsRepository statisticsRepository;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = BadInputException.class)
    public void givenNullDnaWhenCheckingMutantThenExceptionIsThrown() {
        fixture.isMutant(null);
    }

    @Test(expected = BadInputException.class)
    public void givenEmptyDnaWhenCheckingMutantThenExceptionIsThrown() {
        fixture.isMutant(new String[]{});
    }

    @Test
    public void givenDnaWithLessNumberOfCharactersWhenCheckingMutantThenReturnFalse() {
        assertFalse(fixture.isMutant(new String[]{"aaa","aaa","aaa"}));
    }

    @Test(expected = BadInputException.class)
    public void givenDnaWithIncoherentNumberOfCharactersWhenCheckingMutantThenReturnFalse() {
        assertFalse(fixture.isMutant(new String[]{"aaa","aa","aaa"}));
    }

    @Test(expected = BadInputException.class)
    public void givenDnaWithWrongNumberOfStringsWhenCheckingMutantThenExceptionIsThrown() {
        fixture.isMutant(new String[]{"aaaa","aaaa","aaaa"});
    }

    @Test(expected = BadInputException.class)
    public void givenDnaWithInvalidCharactersWhenCheckingMutantThenExceptionIsThrown() {
        fixture.isMutant(new String[]{"atcg","gcta","agtc","agja"});
    }

    @Test
    public void givenDnaWithHorizontalConsecutiveSeqWhenCheckingMutantThenReturnTrue() {
        assertTrue(fixture.isMutant(new String[]{"atcgaa","gctttt","agtcca","agtaga","atcgaa","accgca"}));
        assertTrue(fixture.isMutant(new String[]{"atcgaa","gcttat","acccca","agtaga","atcgaa","accgca"}));
        assertTrue(fixture.isMutant(new String[]{"aaaaat","gcttat","accgca","agtaga","atcgaa","accgca"}));
    }

    @Test
    public void givenDnaWithVerticalConsecutiveSeqWhenCheckingMutantThenReturnTrue() {
        assertTrue(fixture.isMutant(new String[]{"atcgaa","acttat","agtcca","agtaga","atcgaa","accgca"}));
        assertTrue(fixture.isMutant(new String[]{"atcgaa","gcttat","accgaa","agtaaa","atcgaa","accgca"}));
        assertTrue(fixture.isMutant(new String[]{"aatcat","gcttat","accgca","agcaga","atcgaa","accgca"}));
    }

    @Test
    public void givenDnaWithDiagonalConsecutiveSeqWhenCheckingMutantThenReturnTrue() {
        assertTrue(fixture.isMutant(new String[]{
                "atcgaa",
                "cattat",
                "agacca",
                "cgtaga",
                "gtcgaa",
                "accgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "atcgaa",
                "ccttat",
                "acacca",
                "cgcaga",
                "gtccaa",
                "accgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "atcgaa",
                "ccttat",
                "atatca",
                "cgcata",
                "gtccaa",
                "accgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "ctcgaa",
                "gattat",
                "acagaa",
                "tgtaca",
                "atcgag",
                "accgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "ctcgaa",
                "gattat",
                "acagta",
                "tgttca",
                "attgag",
                "atcgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "ctcgaa",
                "gattat",
                "actgta",
                "tttcca",
                "tttgag",
                "atcgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "atcga",
                "catta",
                "agacc",
                "cgtag",
                "gtcga"}));
        assertTrue(fixture.isMutant(new String[]{
                "attat",
                "cagaa",
                "gtaca",
                "tcgag",
                "ccgca"}));
        assertTrue(fixture.isMutant(new String[]{
                "atcg",
                "catt",
                "agac",
                "cgta"}));
        assertTrue(fixture.isMutant(new String[]{
                "gtat",
                "agaa",
                "taga",
                "cgag"}));

    }

    @Test
    public void givenNullMutantCountAndHumanCountWhenGetStatsThenReturnZeroedValues() {
        when(statisticsRepository.findByName(eq(MUTANT_DNA))).thenReturn(null);
        when(statisticsRepository.findByName(eq(HUMAN_DNA))).thenReturn(null);
        StatisticsResponse response = fixture.getRequestsStatistics();
        assertNotNull(response);
        assertEquals(0L, response.getCountMutantDna().longValue());
        assertEquals(0L, response.getCountHumanDna().longValue());
        assertNull(response.getRatio());
    }

    @Test
    public void givenNonNullMutantCountAndHumanCountWhenGetStatsThenReturnCountsAndRatio() {
        when(statisticsRepository.findByName(eq(MUTANT_DNA)))
                .thenReturn(new Statistics(MUTANT_DNA, 40L));
        when(statisticsRepository.findByName(eq(HUMAN_DNA)))
                .thenReturn(new Statistics(HUMAN_DNA, 100L));
        StatisticsResponse response = fixture.getRequestsStatistics();
        assertNotNull(response);
        assertEquals(40L, response.getCountMutantDna().longValue());
        assertEquals(100L, response.getCountHumanDna().longValue());
        assertEquals(0.4d, response.getRatio(), 0d);
    }

}
