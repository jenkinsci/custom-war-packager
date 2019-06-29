package io.jenkins.tools.warpackager.lib.impl.plugins;

import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.model.plugins.PluginInfoProvider;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateCenterPluginInfoProvider implements PluginInfoProvider {

    public static final String DEFAULT_JENKINS_UC_URL = "https://updates.jenkins.io/update-center.json";
    private static final Logger LOGGER = Logger.getLogger(UpdateCenterPluginInfoProvider.class.getName());

    private String updateCenterUrl;
    private Map<String, String> groupIdCache;

    public UpdateCenterPluginInfoProvider(@Nonnull String updateCenterUrl) {
        this.updateCenterUrl = updateCenterUrl;
    }

    @Override
    public void init() throws IOException, InterruptedException {
        groupIdCache = extractUpdateCenterData(new URL(updateCenterUrl));
    }

    @Override
    public boolean isPlugin(@Nonnull DependencyInfo dependency) throws IOException, InterruptedException {
        boolean isPlugin = groupIdCache.containsKey(dependency.artifactId);
        LOGGER.log(Level.FINE, "Checking whether {0} is a plugin: {1}",
                new Object[] {dependency.artifactId, isPlugin});
        return isPlugin;
    }

    private static Map<String, String> extractUpdateCenterData(URL url) throws IOException {
        Map<String, String> groupIDs = new HashMap<>();
        String jsonp = null;
        try {
            jsonp = IOUtils.toString(url.openStream());
        } catch(IOException e){
            throw new IOException("Invalid update center url : " + url, e);
        }

        String json = jsonp.substring(jsonp.indexOf('(')+1,jsonp.lastIndexOf(')'));
        JSONObject jsonObj = JSONObject.fromObject(json);

        // UpdateSite.Plugin does not contain gav object, so we process the JSON object on our own here
        for(Map.Entry<String,JSONObject> e : (Set<Map.Entry<String,JSONObject>>)jsonObj.getJSONObject("plugins").entrySet()) {
            String gav = e.getValue().getString("gav");
            String groupId = gav.split(":")[0];
            groupIDs.putIfAbsent(e.getKey(), groupId);
        }
        return groupIDs;
    }
}
