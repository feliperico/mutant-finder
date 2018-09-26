package meli.magneto.mutantfinder;

public interface MutantFinderService {

    boolean isMutant(String[] dna);

    StatisticsResponse getRequestsStatistics();
}
