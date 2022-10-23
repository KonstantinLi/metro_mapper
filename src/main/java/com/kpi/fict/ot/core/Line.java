package com.kpi.fict.ot.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO class that stores line information.
 * @version 1.0-SNAPSHOT
 * @author Linenko Kostyantyn, student of IO-01 in KPI
 */
@Data
public class Line implements Comparable<Line>
{
    /** Number. */
    private int number;
    /** Name. */
    private String name;
    /** List of stations. */
    private List<Station> stations;

    /**
     * A single constructor.
     * @param number number
     * @param name name
     */
    public Line(int number, String name)
    {
        this.number = number;
        this.name = name;
        stations = new ArrayList<>();
    }

    /**
     * Adds a station.
     * @param station the station
     */
    public void addStation(Station station)
    {
        stations.add(station);
    }

    /**
     * Compares the current line with the passed line by number.
     * @param line the object to be compared.
     * @return int
     */
    @Override
    public int compareTo(Line line)
    {
        return Integer.compare(number, line.getNumber());
    }

    /**
     * Equal or not equal to the passed object.
     * @param obj the object
     * @return true or false
     */
    @Override
    public boolean equals(Object obj)
    {
        return compareTo((Line) obj) == 0;
    }
}