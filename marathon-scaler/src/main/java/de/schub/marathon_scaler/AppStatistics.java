package de.schub.marathon_scaler;

import java.util.Optional;

public class AppStatistics
{
    Optional<Float> cpu;
    Optional<Float> memory;
    Optional<Float> diskUsage;

    public Optional<Float> getCpu()
    {
        return cpu;
    }

    public void setCpu(Optional<Float> cpu)
    {
        this.cpu = cpu;
    }

    public Optional<Float> getMemory()
    {
        return memory;
    }

    public void setMemory(Optional<Float> memory)
    {
        this.memory = memory;
    }

    public Optional<Float> getDiskUsage()
    {
        return diskUsage;
    }

    public void setDiskUsage(Optional<Float> diskUsage)
    {
        this.diskUsage = diskUsage;
    }
}