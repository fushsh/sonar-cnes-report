package fr.cnes.sonar.report.providers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.cnes.sonar.report.exceptions.UnknownParameterException;
import fr.cnes.sonar.report.model.Facet;
import fr.cnes.sonar.report.model.Issue;
import fr.cnes.sonar.report.params.Params;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides issue items
 * @author begarco
 */
public class IssuesProvider implements IDataProvider {

    /**
     * Logger for the class
     */
    private static final Logger LOGGER = Logger.getLogger(IssuesProvider.class.getCanonicalName());

    /**
     * Params of the program itself
     */
    private Params params;

    public IssuesProvider(Params params) {
        this.setParams(params);
    }

    /**
     * Get all the issues of a project
     * @return Array containing all the issues
     */
    public List<Issue> getIssues() throws IOException, UnknownParameterException {
        // results variable
        ArrayList<Issue> res = new ArrayList<>();

        // stop condition
        boolean goon = true;
        // current page
        int page = 1;

        // json tool
        Gson gson = new Gson();
        // get sonar url
        String url = getParams().get("sonar.url");
        // get project key
        String projectKey = getParams().get("sonar.project.id");

        // search all issues of the project
        while(goon) {
            String request = String.format("%s/api/issues/search?projectKeys=%s&resolved=false&facets=types,rules,severities,directories,fileUuids,tags&ps=%d&p=%d&additionalFields=rules",
                    url, projectKey, IDataProvider.MAX_PER_PAGE_SONARQUBE, page);
            String raw = RequestManager.getInstance().get(request);
            JsonElement json = gson.fromJson(raw, JsonElement.class);
            JsonObject jo = json.getAsJsonObject();
            Issue [] tmp = (gson.fromJson(jo.get("issues"), Issue[].class));
            res.addAll(Arrays.asList(tmp));
            int number = (json.getAsJsonObject().get("total").getAsInt());
            goon = page*IDataProvider.MAX_PER_PAGE_SONARQUBE < number;
            page++;
        }

        // return the issues
        return res;
    }

    /**
     * Get all the stats on a project
     * @return A list of facets
     * @throws IOException on data processing error
     * @throws UnknownParameterException on bad parameter
     */
    public List<Facet> getFacets() throws IOException, UnknownParameterException {
        // results variable
        ArrayList<Facet> res = new ArrayList<>();

        // json tool
        Gson gson = new Gson();
        // get sonar url
        String url = getParams().get("sonar.url");
        // get project key
        String projectKey = getParams().get("sonar.project.id");

        // prepare the request
        String request =
                String.format("%s/api/issues/search?projectKeys=%s&resolved=false&facets=rules,severities,types&ps=1&p=1",
                url, projectKey);
        // apply the request
        String raw = RequestManager.getInstance().get(request);
        // prepare json
        JsonElement json = gson.fromJson(raw, JsonElement.class);
        JsonObject jo = json.getAsJsonObject();
        // put wanted data in facets array and list
        Facet [] tmp = (gson.fromJson(jo.get("facets"), Facet[].class));
        res.addAll(Arrays.asList(tmp));

        // return list of facets
        return res;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }
}
