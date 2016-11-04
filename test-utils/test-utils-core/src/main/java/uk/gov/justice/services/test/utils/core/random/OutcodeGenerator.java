package uk.gov.justice.services.test.utils.core.random;

import java.util.Random;

/**
 * 
 * This class is used to generate the first part of the Postcode
 * 
 * <ul>
 * <li>The letters Q, V and X are not used in the first position</li>
 * <li>The letters I,J and Z are not used in the second position.</li>
 * <li>The only letters to appear in the third position are A, B, C, D, E, F, G, H, J, K, S, T, U
 * and W.</li>
 * </ul>
 * 
 */
public class OutcodeGenerator {

    /**
     * Letters that are valid in the first position
     */
    private static final String VALID_FIRST_LETTER = "ABCDEFGHIJKLMNOPRSTUWYZ";

    /**
     * Letters that are valid in the second position
     */
    private static final String VALID_SECOND_LETTER = "ABCDEFGHKLMNOPQRSTUVWXY";

    /**
     * Letters that are valid in the third position
     */
    private static final String VALID_THIRD_LETTER = "ABCDEFGHJKSTUW";

    /**
     * List of formats
     */
    private static final String[] FORMATS =
                    new String[] {"AN", "ANN", "AAN", "AANN", "ANA", "AANA"};

    /**
     * Random generator
     */
    private final Random random;

    /**
     * Package specific to prevent instantiation
     */
    OutcodeGenerator(final Random random) {
        this.random = random;
    }

    /**
     * Get the outcode for the postcode
     * 
     * @return String
     */
    public String next() {

        final int lengthValidFirstLetter = VALID_FIRST_LETTER.length();
        final int lengthValidSecondLetter = VALID_SECOND_LETTER.length();
        final int lengthValidThirdLetter = VALID_THIRD_LETTER.length();
                        
        final String selectedFormat = FORMATS[random.nextInt(FORMATS.length)];
        
        final String[] selectedFormatTokens = selectedFormat.split("");

        final StringBuilder sb = new StringBuilder();

        int alphaIndex = 0;
        
        for (int i = 0; i < selectedFormatTokens.length; i++) {
            
            final String selectedFormatCharacter = selectedFormatTokens[i];
            
            switch (selectedFormatCharacter) {
        
                case "A":                    
                    switch (alphaIndex) {
                        case 0:
                            sb.append(VALID_FIRST_LETTER
                                            .charAt(random.nextInt(lengthValidFirstLetter)));
                            break;
                        case 1:
                            sb.append(VALID_SECOND_LETTER
                                            .charAt(random.nextInt(lengthValidSecondLetter)));
                            break;
                        case 2:
                            sb.append(VALID_THIRD_LETTER
                                            .charAt(random.nextInt(lengthValidThirdLetter)));
                            break;
                    };
                
                    alphaIndex++;
                    
                    break;
                
                case "N":
                    
                    sb.append(random.nextInt(10));
                    
                    break;
            }
        }


        return sb.toString();
    }

}
