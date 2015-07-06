package de.schub.marathon_scaler.Customer;

import com.google.gson.Gson;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupTemplate
{
    private final Gson gson;
    private final String json;

    public GroupTemplate(Gson gson, String json)
    {
        this.gson = gson;
        this.json = json;
    }

    public Group create(Group oldGroup, Customer customer)
    {
        String json2 = json.replace("{customer_id}", Integer.toString(customer.getId()));
        Group group = gson.fromJson(json2, Group.class);
        group.setId("/customer/" + customer.getId());

        return merge(group, oldGroup);
    }

    /**
     * merge two groups with all apps to keep instance count, cpu and memory limitations
     * @param newGroup
     * @param oldGroup
     * @return
     */
    private Group merge(Group newGroup, Group oldGroup)
    {
        Map<String, App> appMap;

        // merge sub groups
        if (null != oldGroup) {
            Map<String, Group> oldGroupMap = oldGroup.getGroups()
                .stream()
                .collect(Collectors.toMap(Group::getId, group -> group));
            newGroup.getGroups().stream().map(group -> merge(group, oldGroupMap.get(group.getId())));

            appMap = oldGroup.getApps()
                .stream()
                .collect(Collectors.toMap(App::getId, app -> app));
        } else {
             appMap = new HashMap<>();
        }

        // merge apps
        newGroup.getApps().stream().map(app -> merge(app, appMap.get(app.getId())));

        return newGroup;
    }

    private App merge(App newApp, App oldApp)
    {
        if (null == oldApp) {
            return newApp;
        }
        newApp.setInstances(oldApp.getInstances());
        newApp.setCpus(oldApp.getCpus());
        newApp.setMem(oldApp.getMem());

        return newApp;
    }
}
