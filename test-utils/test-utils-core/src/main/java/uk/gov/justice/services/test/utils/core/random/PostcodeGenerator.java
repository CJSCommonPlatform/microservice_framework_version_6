package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;

/**
 * Sources <br>
 * http://www.postcodeaddressfile.co.uk/products/postcodes/postcodes_explained.htm <br>
 * https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/283357/ILRSpecification2013_14Appendix_C_Dec2012_v1.pdf
 * 
 * <br>
 * Postcode is made up of
 * <ol>
 * <li>outward postcode</li>
 * <li>space</li>
 * <li>inward postcode</li>
 * </ol>
 * 
 * <p>
 * The outward postcode enables mail to be sent to the correct <br>
 * local area for delivery. <br>
 * This part of the code contains the area and the district to which <br>
 * the mail is to be delivered. <br>
 * </p>
 * <p>
 * The inward postcode is used to sort the mail at the local area <br>
 * delivery office. <br>
 * It consists of a numeric character followed by two alphabetic <br>
 * characters. The numeric character identifies the sector within <br>
 * the postal district. The alphabetic characters then define one <br>
 * or more properties within the sector. <br>
 * </p>
 * 
 * Example: PO1 3AX
 * <p>
 * PO refers to the postcode area of Portsmouth <br>
 * There are 124 postcode areas in the UK
 * </p>
 * 
 * <p>
 * PO1 refers to a postcode district within the postcode area of <br>
 * Portsmouth. <br>
 * There are approximately 2,900 postcode districts.
 * </p>
 * 
 * <p>
 * PO1 3 refers to the postcode sector. <br>
 * There are approximately 9,650 postcode sectors.
 * </p>
 * 
 * <p>
 * PO1 3AX. The AX completes the postcode. The last two letters define the <br>
 * ‘unit postcode’ which identifies one or more small user delivery points <br>
 * or an individual large user.
 * </p>
 * 
 * <p>
 * There are approximately 1.71 million unit postcodes in the UK.
 * </p>
 * 
 * <p>
 * Valid Formats: A indicates an alphabetic character and N indicates a numeric character
 * </p>
 * 
 * <table>
 * <tr>
 * <th>Outcode</th>
 * <th>Incode</th>
 * <th>Example Postcode</th>
 * </tr>
 * <tr>
 * <td>AN</th>
 * <th>NAA</th>
 * <th>M1 1AA</th>
 * </tr>
 * <tr>
 * <td>ANN</th>
 * <th>NAA</th>
 * <th>M10 1NW</th>
 * </tr>
 * <tr>
 * <td>AAN</th>
 * <th>NAA</th>
 * <th>CR2 6NY</th>
 * </tr>
 * <tr>
 * <td>AANN</th>
 * <th>NAA</th>
 * <th>CN65 1AP</th>
 * </tr>
 * <tr>
 * <td>ANA</th>
 * <th>NAA</th>
 * <th>M1P 1AA</th>
 * </tr>
 * <tr>
 * <td>AANA</th>
 * <th>NAA</th>
 * <th>EC1A 1AA</th>
 * </tr>
 * </table>
 * 
 * <p>
 * The following characters are never used in the <b>inward part</b> of the postcode. <br>
 * CIKMOV
 * </p>
 * 
 * <p>
 * 
 * <ul>
 * <li>The letters Q, V and X are not used in the first position</li>
 * <li>The letters I,J and Z are not used in the second position.</li>
 * <li>The only letters to appear in the third position are A, B, C, D, E, F, G, H, J, K, S, T, U
 * and W.</li>
 * </ul>
 * </p>
 * 
 */
public class PostcodeGenerator extends Generator<String> {


    final IncodeGenerator incodeGenerator;
    final OutcodeGenerator outcodeGenerator;

    /**
     * Package specific to prevent instantiation
     */
    PostcodeGenerator() {
        this.incodeGenerator = new IncodeGenerator(RANDOM);
        this.outcodeGenerator = new OutcodeGenerator(RANDOM);
    }

    @Override
    public String next() {
        return format("%s %s", outcodeGenerator.next(), incodeGenerator.next());
    }
}
