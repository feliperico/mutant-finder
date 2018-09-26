package meli.magneto.mutantfinder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MutantFinderController.class)
public class MutantFinderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
    private MutantFinderService mutantFinderService;

	private HttpHeaders httpHeaders;

	private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

	@Before
    public void init() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
    }

	@Test
	public void testBadRequestDueToNullInput() throws Exception {
		DnaRequest dnaRequest = new DnaRequest();
		String jsonContent = writeValueAsString(dnaRequest);
		when(mutantFinderService.isMutant(eq(null))).thenThrow(new BadInputException("any"));
		this.mockMvc.perform(post("/mutant/").headers(httpHeaders).content(jsonContent))
                .andDo(print()).andExpect(status().isBadRequest());
	}

    @Test
    public void testForbiddenResponseDueToNonMutantDna() throws Exception {
        DnaRequest dnaRequest = new DnaRequest();
        dnaRequest.setDna(new String[]{
                "atcgaa",
                "cattat",
                "agccca",
                "cgtaga",
                "gtcgaa",
                "accgca"});
        when(mutantFinderService.isMutant(eq(dnaRequest.getDna()))).thenReturn(Boolean.FALSE);
        String jsonContent = writeValueAsString(dnaRequest);
        this.mockMvc.perform(post("/mutant/").headers(httpHeaders).content(jsonContent))
                .andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    public void testOkResponseDueToNonMutantDna() throws Exception {
        DnaRequest dnaRequest = new DnaRequest();
        dnaRequest.setDna(new String[]{
                "atcgaa",
                "cactat",
                "agccca",
                "cgtaca",
                "gtcgac",
                "accgca"});
        when(mutantFinderService.isMutant(eq(dnaRequest.getDna()))).thenReturn(Boolean.TRUE);
        String jsonContent = writeValueAsString(dnaRequest);
        this.mockMvc.perform(post("/mutant/").headers(httpHeaders).content(jsonContent))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testStatisticsResponseOnGettingStats() throws Exception {
	    StatisticsResponse response = new StatisticsResponse();
	    response.setCountMutantDna(40L);
	    response.setCountHumanDna(100L);
	    response.setRatio(0.4d);
        when(mutantFinderService.getRequestsStatistics()).thenReturn(response);
        this.mockMvc.perform(get("/stats/").headers(httpHeaders))
                .andDo(print())
                .andExpect(jsonPath("$.count_mutant_dna", is(40)))
                .andExpect(jsonPath("$.count_human_dna", is(100)))
                .andExpect(jsonPath("$.ratio", is(0.4)))
                .andExpect(status().isOk());
    }

	public static String writeValueAsString(Object value) throws JsonProcessingException {
		return DEFAULT_MAPPER.writeValueAsString(value);
	}

}
