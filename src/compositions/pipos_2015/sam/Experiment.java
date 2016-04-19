package compositions.pipos_2015.sam;

import java.io.File;
import java.util.Hashtable;

/**
 * Created by samferguson on 24/12/2015.
 */
public class Experiment {

    Hashtable<String, Hashtable> IndependentVariables;
    Hashtable<String, Object> DependentVariables;
    Hashtable<Integer, String> Combinations;
    Hashtable<String, String> SurveyInformation;

    File filename;

    public Experiment(){
        filename = new File("dfsdfsdf.txt");
    }


    public boolean valid(){

        return false;
    }


    public void writeTextFile(){

        //List<String> lines = Arrays.asList("The first line", "The second line");
        //Path file = Paths.get("the-file-name.txt");
        //Files.write(file, lines, Charset.forName("UTF-8"));
        //Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);

    }

    private void setupTextFile(){
        /*
        Combinations of all independent variables in every are setup and randomised and then loaded into 'Combinations'.
        Then the Combinations are saved into a text file alongside information about whether the combination has been completed yet.
        These tables are saved with placeholders for the Dependent Variables, alongside all the extra demographic information requested

        // Find all the combinations

        // For each IV find the hashtable
        // for each position in the hashtable create a 1) name of position 2) value for the position (keep for later
        // create two columns for the variable

        // For each DV find the hashtable
        // for each position in the hashtable create a column for the variable
        // set the column type based on the type of the recorded data

        // Once all the columns are created look at the set of IV positions and
        // Create a row for each combination of the positions systematically

        // Randomise the combination order and setup a file for each respondent.

        // Create a companion text file that has fields for each of the demographics
        // For each of the hashtable positions
        // Create a row with the responses.
        // Create a

        // Create an R based file reading script to assist in loading the data.
        // Create an Rmarkdown file that will generate a textual description of the IVs and the DV data.
        // Tabulate positions and repeats
        // Tabulate the number of repeats necessary

        // Create a text file

        */


    }





}
