package com.kpi.fict.ot.core;

import lombok.Data;

/**
 * POJO-class that stores information about the station.
 * @version 1.0-SNAPSHOT
 * @author Linenko Kostyantyn, student of IO-01 in KPI
 */
@Data
public class Station implements Comparable<Station>
{
    /** Line. */
    private Line line;
    /** Name of station. */
    private String name;

    /**
     * A single constructor.
     * @param name the name
     * @param line the line
     */
    public Station(String name, Line line)
    {
        this.name = name;
        this.line = line;
    }

    /**
     * Compares the current station with the transmitted station by line number and name.
     * @param station the object to be compared.
     * @return int
     */
    @Override
    public int compareTo(Station station)
    {
        int lineComparison = line.compareTo(station.getLine());
        if(lineComparison != 0) {
            return lineComparison;
        }
        return name.compareToIgnoreCase(station.getName());
    }

    /**
     * Is equal or not equal to the object.
     * @param obj the object
     * @return true or false
     */
    @Override
    public boolean equals(Object obj)
    {
        return compareTo((Station) obj) == 0;
    }

    /**
     * Term representation.
     * @return station name
     */
    @Override
    public String toString()
    {
        return name;
    }
}