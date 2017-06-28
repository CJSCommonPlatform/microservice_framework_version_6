package uk.gov.justice.services.test.utils.core.random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.isIn;

import org.junit.Test;

public class RandomPersonNameGeneratorTest {

    private static final String[] GIVEN_NAMES = new String[]{
            "Ada", "Albert", "Alexandra", "Alfredo", "Allen", "Andre", "Angelica",
            "Anna", "Anthony", "Antonio", "Ashley", "Audrey", "Beatrice",
            "Benjamin", "Billy", "Bobby", "Bradley", "Bryant", "Candace",
            "Carole", "Carrie", "Claire", "Clifford", "Clint", "Clyde", "Cory",
            "Dale", "Danielle", "Daryl", "Delia", "Devin", "Douglas", "Eddie",
            "Ella", "Erica", "Erika", "Eva", "Frank", "Gayle", "George", "Georgia",
            "Geraldine", "Gina", "Gwen", "Hector", "Homer", "Irene", "James",
            "Jamie", "Jeremiah", "Joann", "Josefina", "Juan", "Karen", "Kenneth",
            "Laurie", "Lee", "Leland", "Leroy", "Levi", "Lewis", "Lillian",
            "Lillie", "Lorenzo", "Louise", "Lucas", "Lynn", "Marc", "Marcella",
            "Marlon", "Marvin", "Micheal", "Miranda", "Miriam", "Misty", "Naomi",
            "Natasha", "Nelson", "Oliver", "Pete", "Rafael", "Randall", "Raul",
            "Rebecca", "Reginald", "Roger", "Ruby", "Rufus", "Sabrina", "Sean",
            "Steven", "Stuart", "Terence", "Terry", "Van", "Velma", "Vincent",
            "Wanda", "Willard", "Winifred"
    };

    private static final String[] FAMILY_NAMES = new String[]{
            "Adkins", "Aguilar", "Anderson", "Armstrong", "Arnold", "Bailey",
            "Banks", "Barrett", "Bates", "Bennett", "Bowers", "Bradley", "Brown",
            "Bryant", "Buchanan", "Bush", "Butler", "Cain", "Carlson", "Carroll",
            "Cummings", "Diaz", "Doyle", "Duncan", "Dunn", "Fernandez", "Foster",
            "Fowler", "Fox", "Francis", "French", "Garrett", "Gill", "Glover",
            "Goodwin", "Gordon", "Grant", "Griffin", "Gross", "Guerrero", "Hale",
            "Harvey", "Holland", "Ingram", "Jacobs", "James", "Lamb", "Lowe",
            "Lucas", "Mann", "Marshall", "Martin", "Martinez", "May", "Mcdaniel",
            "Mendoza", "Meyer", "Moody", "Moreno", "Nelson", "Nichols", "Norton",
            "Obrien", "Osborne", "Padilla", "Page", "Parks", "Parsons", "Payne",
            "Pearson", "Powell", "Reese", "Reeves", "Reyes", "Reynolds",
            "Richardson", "Rios", "Ross", "Russell", "Saunders", "Sharp", "Simon",
            "Smith", "Steele", "Stephens", "Stokes", "Summers", "Thomas",
            "Thompson", "Tyler", "Wagner", "Ward", "Washington", "Watkins",
            "Watson", "Weber", "West", "Willis", "Young", "Zimmerman"
    };

    private static final String[] TITLES = new String[]{
            "Mr", "Mrs", "Ms", "Mx", "Miss", "Master"
    };

    @Test
    public void shouldPickARandomGivenNameFromTheList() {
        assertThat(RandomPersonNameGenerator.givenName(), isIn(GIVEN_NAMES));
    }

    @Test
    public void shouldPickARandomFamilyNameFromTheList() {
        assertThat(RandomPersonNameGenerator.familyName(), isIn(FAMILY_NAMES));
    }

    @Test
    public void shouldPickARandomTitleFromTheList() {
        assertThat(RandomPersonNameGenerator.title(), isIn(TITLES));
    }

}