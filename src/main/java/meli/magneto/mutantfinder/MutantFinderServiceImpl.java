package meli.magneto.mutantfinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static java.lang.Character.toLowerCase;
import static meli.magneto.mutantfinder.Statistics.HUMAN_DNA;
import static meli.magneto.mutantfinder.Statistics.MUTANT_DNA;

@Service
public class MutantFinderServiceImpl implements MutantFinderService {
    private static int SEQUENCE_SIZE = 4;
    private static String VALID_DNA_CHARACTERS = "atcgATCG";

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Override
    public boolean isMutant(String[] dna) {
        // validate input
        validateDnaInput(dna);

        boolean isMutant = false;

        if (dna.length >= SEQUENCE_SIZE) {
            // horizontal check
            isMutant = checkHorizontalSequence(dna);

            // vertical check
            if (!isMutant) {
                isMutant = checkVerticalSequence(dna);
            }

            // diagonal check
            if (!isMutant) {
                isMutant = checkDiagonalSequence(dna);
            }
        }

        // stores the result in the database
        String statName = isMutant ? MUTANT_DNA : HUMAN_DNA;
        Statistics statistics = statisticsRepository.findByName(statName);
        if (statistics == null) {
            statistics = new Statistics(statName, 1L);
        } else {
            statistics.setAmount(statistics.getAmount() + 1L);
        }
        statisticsRepository.save(statistics);

        return isMutant;
    }

    private void validateDnaInput(String[] dna) throws BadInputException {
        if (dna == null) {
            throw new BadInputException("Dna cannot be null");
        }
        if (dna.length == 0) {
            throw new BadInputException("Dna cannot be empty");
        }
        int length = dna.length;
        boolean inconsistentSeq = Arrays.stream(dna)
                .anyMatch(s -> s == null || s.length() != length || !validDnaString(s));
        if (inconsistentSeq) {
            throw new BadInputException("Dna has inconsistent sequences");
        }
    }

    /**
     * Verifies if all characters are valid DNA characters (ATCG)
     * @param curr string being tested
     * @return <tt>true</tt> if valid or <tt>false</tt> if a wrong char is detected
     */
    private boolean validDnaString(String curr) {
        return curr.chars()
                .allMatch(ch -> VALID_DNA_CHARACTERS.contains(Character.toString((char)ch)));
    }

    private boolean checkHorizontalSequence(String[] dna) {
        return Arrays.stream(dna).anyMatch(this::hasMutantSequence);
    }

    private boolean hasMutantSequence(String s) {
        char prev = s.charAt(0);
        int prevCounter = 1;
        for (int i = 1; i < s.length() && (s.length() - i + prevCounter) >= SEQUENCE_SIZE; i++) {
            char curr = s.charAt(i);
            if (toLowerCase(prev) == toLowerCase(curr)) {
                prevCounter++;
            } else {
                prevCounter = 1;
            }
            if (prevCounter == SEQUENCE_SIZE) {
                return true;
            } else {
                prev = curr;
            }
        }
        return false;
    }

    private boolean checkVerticalSequence(String[] dna) {
        int strLength = dna.length;
        for (int charIdx = 0; charIdx < strLength; charIdx++) {
            StringBuilder sbSequence = new StringBuilder();
            for (String aDna : dna) {
                sbSequence.append(aDna.charAt(charIdx));
            }
            if (hasMutantSequence(sbSequence.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalSequence(String[] dna) {
        StringBuilder sbSequence;
        /*
        First checks left to right diagonals
         */
        // Checks top half diagonals
        for(int k = 0; k <= dna.length - SEQUENCE_SIZE; k++) {
            sbSequence = new StringBuilder();
            for(int j = 0 ; j <= dna.length - k - 1; j++) {
                int i = k + j;
                sbSequence.append(dna[i].charAt(j));
            }
            if (hasMutantSequence(sbSequence.toString())) {
                return true;
            }
        }

        // Checks bottom half diagonals
        for(int k = SEQUENCE_SIZE - 2; k > 0; k--) {
            sbSequence = new StringBuilder();
            for(int j = 0 ; j <= dna.length - k - 1; j++) {
                int i = k + j;
                sbSequence.append(dna[i].charAt(j));
            }
            if (hasMutantSequence(sbSequence.toString())) {
                return true;
            }
        }

        /*
        Second right to left diagonals
         */
        // Checks top half diagonals
        for(int k = SEQUENCE_SIZE - 1; k < dna.length; k++) {
            sbSequence = new StringBuilder();
            for(int j = 0 ; j <= k ; j++) {
                int i = k - j;
                sbSequence.append(dna[i].charAt(j));
            }
            if (hasMutantSequence(sbSequence.toString())) {
                return true;
            }
        }

        // Checks bottom half diagonals
        for (int k = dna.length - 2; k >= SEQUENCE_SIZE - 1; k--) {
            sbSequence = new StringBuilder();
            for (int j = 0; j <= k; j++) {
                int i = k - j;
                sbSequence.append(dna[dna.length - j - 1].charAt(dna.length - i - 1));
            }
            if (hasMutantSequence(sbSequence.toString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public StatisticsResponse getRequestsStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        Statistics mutantStats = statisticsRepository.findByName(MUTANT_DNA);
        if (mutantStats != null) {
            response.setCountMutantDna(mutantStats.getAmount());
        } else {
            response.setCountMutantDna(0L);
        }
        Statistics humanStats = statisticsRepository.findByName(HUMAN_DNA);
        if (humanStats != null) {
            response.setCountHumanDna(humanStats.getAmount());
        } else {
            response.setCountHumanDna(0L);
        }
        // ratio only make sense when both numbers are non-zero
        if (response.getCountHumanDna() > 0L && response.getCountMutantDna() > 0L) {
            response.setRatio(response.getCountMutantDna().doubleValue() / response.getCountHumanDna().doubleValue());
        }
        return response;

    }
}
