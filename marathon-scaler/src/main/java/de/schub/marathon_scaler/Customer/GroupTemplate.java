package de.schub.marathon_scaler.Customer;

import com.google.gson.Gson;
import de.schub.marathon_scaler.Monitoring.MarathonMonitor;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupTemplate
{
    private final Gson gson;
    private final String templateJson;

    public GroupTemplate(Gson gson, String templateJson)
    {
        this.gson = gson;
        this.templateJson = templateJson;
    }

    public Group create(Customer customer, Group oldGroup)
    {
        String json2 = templateJson.replace("{customer_id}", Integer.toString(customer.getId()));
        Group group = gson.fromJson(json2, Group.class);
        group.setId("/customer/" + customer.getId());
        merge(customer, group, oldGroup);

        return group;
    }

    /**
     * merge two groups with all apps to keep instance count, cpu and memory limitations
     *
     * @param customer
     * @param newGroup
     * @param oldGroup
     * @return
     */
    private Group merge(Customer customer, Group newGroup, Group oldGroup)
    {
        Map<String, App> appMap;
        Map<String, Group> oldGroupMap;
        if (null != oldGroup) {
            oldGroupMap = oldGroup.getGroups()
                .stream()
                .collect(Collectors.toMap(Group::getId, group -> group));
        } else {
            oldGroupMap = new HashMap<>();
        }
        // merge sub groups
        if (null != newGroup.getGroups()) {
            newGroup.getGroups()
                .stream()
                .map(group -> merge(customer, group, oldGroupMap.get(group.getId())));
        }

        if (null != oldGroup && null != oldGroup.getApps()) {
            appMap = oldGroup.getApps()
                .stream()
                .collect(Collectors.toMap(App::getId, app -> app));
        } else {
            appMap = new HashMap<>();
        }

        // merge apps
        if (null != newGroup.getApps()) {
            for (App app : newGroup.getApps()) {
                App oldApp;
                // relative name
                if (app.getId().substring(0, 2).equals("./")) {
                    oldApp = appMap.get(
                        newGroup.getId() + "/" + app.getId().substring(2, app.getId().length())
                    );
                } else {
                    oldApp = appMap.get(app.getId());
                }
                mergeApp(customer, app, oldApp);
            }
        }

        return newGroup;
    }

    private void mergeApp(Customer customer, App newApp, App oldApp)
    {
        // add customer id as service tag
        Map<String, String> envVariables = newApp.getEnv();
        if (envVariables.containsKey("SERVICE_TAGS")) {
            envVariables.put("SERVICE_TAGS", envVariables.get("SERVICE_TAGS") + ",customer-" + customer.getId());
        } else {
            envVariables.put("SERVICE_TAGS", "customer-" + customer.getId());
        }

        if (null == oldApp) {
            return;
        }
        // only merge settings for scaled applications
        if (null != newApp.getLabels() && newApp.getLabels().containsKey(MarathonMonitor.LABEL_SCALING_STRATEGY)) {
            return;
        }
        newApp.setInstances(oldApp.getInstances());
        newApp.setCpus(oldApp.getCpus());
        newApp.setMem(oldApp.getMem());
    }
}
