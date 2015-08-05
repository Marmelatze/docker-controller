package de.schub.marathon_scaler.Customer;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.Group;
import mesosphere.marathon.client.utils.MarathonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Listens to changes to the customer prefix and adds or removes marathon groups accordingly.
 */
public class CustomerService
{
    private final CustomerStorage customerStorage;
    private final Marathon marathon;
    Logger logger = LoggerFactory.getLogger(CustomerService.class);

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
        logger.info("Config was updated");
        // always update. marathon will handle new groups and changes to existing groups.
        for (Map.Entry<Integer, Customer> entry : customers.entrySet()) {
            Customer customer = entry.getValue();
            try {
                Group oldGroup = null;
                try {
                    oldGroup = marathon.getGroup("/customer/" + customer.getId());
                    logger.info("Updating existing customer {}", customer.getId());
                } catch (MarathonException e) {
                    // group doesn't exist
                    logger.info("Adding new customer {}", customer.getId());
                }
                Group group = template.create(customer, oldGroup);
                marathon.updateGroup(group.getId(), group, true);
            } catch (MarathonException e) {
                logger.error("Failed to update customer " + customer.getId(), e);
            }
        }

        try {
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
            logger.error("Failed to remove old groups", e);
        }
    }
}
