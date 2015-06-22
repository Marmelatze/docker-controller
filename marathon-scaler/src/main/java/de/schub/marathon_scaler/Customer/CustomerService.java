package de.schub.marathon_scaler.Customer;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.utils.MarathonException;

import java.util.Map;

public class CustomerService
{
    private final CustomerStorage customerStorage;
    private final Marathon marathon;

    public CustomerService(CustomerStorage customerStorage, Marathon marathon)
    {
        this.customerStorage = customerStorage;
        this.marathon = marathon;
    }

    public void run()
    {
        customerStorage.registerEventSubscriber(this::update);
    }

    private void update(GroupTemplate template, Map<Integer, Customer> customers)
    {
        try {
            // always update. marathon will handle new groups and changes to existing
            for (Map.Entry<Integer, Customer> entry : customers.entrySet()) {
                Group group = template.create(entry.getValue());
                marathon.updateGroup(group.getId(), group, true);
            }
            // remove old customers
            Group parentGroup = marathon.getGroup("customer");
            for (Group customerGroup : parentGroup.getGroups()) {
                int customerId = Integer.valueOf(customerGroup.getId().replaceFirst("/customer/", ""));
                if (!customers.containsKey(customerId)) {
                    System.out.println("delete customer " + customerId);
                    marathon.deleteGroup("/customer/" + customerId);
                }
            }
        } catch (MarathonException e) {
            e.printStackTrace();
        }
    }
}
