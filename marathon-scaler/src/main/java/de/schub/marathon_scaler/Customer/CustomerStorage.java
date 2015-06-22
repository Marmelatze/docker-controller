package de.schub.marathon_scaler.Customer;

import com.google.gson.Gson;
import com.orbitz.consul.Consul;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.QueryOptionsBuilder;
import com.orbitz.consul.util.ClientUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.stream.Collectors;

public class CustomerStorage extends Observable
{
    private final Consul consul;
    private final Gson gson;

    @Inject
    public CustomerStorage(Consul consul, Gson gson)
    {
        this.consul = consul;
        this.gson = gson;
    }

    public Customer get(int id)
    {
        return get(consul.keyValueClient().getValueAsString("customer/" + id).get());
    }

    private Customer get(String json)
    {
        return gson.fromJson(json, Customer.class);
    }

    public Map<Integer, Customer> getAll()
    {
        return getFromValues(consul.keyValueClient().getValues("/customer"));
    }

    public GroupTemplate getGroupTemplate()
    {
        return new GroupTemplate(gson, consul.keyValueClient().getValueAsString("customer/template").get());
    }

    private Map<Integer, Customer> getFromValues(List<Value> values)
    {
        return values.stream()
            .filter(value -> value.getKey().matches("^customer/[0-9]+$"))
            .map(
                value -> get(
                    ClientUtil.decodeBase64((value.getValue()))
                )
            )
            .collect(Collectors.toMap(Customer::getId, customer -> customer));
    }

    public void registerEventSubscriber(CustomerEventSubscriber subscriber)
    {
        consul.keyValueClient().getValues(
            "/customer",
            QueryOptionsBuilder.builder().blockMinutes(1, 0).build(),
            new ConsulResponseCallback<List<Value>>()
            {
                long index;

                @Override
                public void onComplete(ConsulResponse<List<Value>> consulResponse)
                {
                    subscriber.onUpdate(getGroupTemplate(), getFromValues(consulResponse.getResponse()));
                    index = consulResponse.getIndex();
                    consul.keyValueClient()
                        .getValues(
                            "/customer",
                            QueryOptionsBuilder.builder().blockMinutes(10, index).build(),
                            this
                        );

                }

                @Override
                public void onFailure(Throwable throwable)
                {
                    throwable.printStackTrace();

                    consul.keyValueClient()
                        .getValues(
                            "/customer",
                            QueryOptionsBuilder.builder().blockMinutes(10, index).build(),
                            this
                        );
                }
            }
        );
    }

    public interface CustomerEventSubscriber
    {
        void onUpdate(GroupTemplate template, Map<Integer, Customer> customers);
    }
}
