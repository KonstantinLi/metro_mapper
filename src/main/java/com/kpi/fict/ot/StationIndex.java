package com.kpi.fict.ot;

import com.kpi.fict.ot.core.Line;
import com.kpi.fict.ot.core.Station;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container class for stations, lines and connections.
 * @version 1.0-SNAPSHOT
 * @author Linenko Kostyantyn, student of IO-01 in KPI
 */
public class StationIndex {
    private final Map<Integer, Line> number2line;
    private final TreeSet<Station> stations;
    private final Map<Station, TreeSet<Station>> connections;

    /**
     * A single constructor with no arguments.
     */
    public StationIndex() {
        number2line = new HashMap<>();
        stations = new TreeSet<>();
        connections = new TreeMap<>();
    }

    /**
     * Adds a station.
     * @param station the station
     */
    public void addStation(Station station) {
        stations.add(station);
    }

    /**
     * Adds a line.
     * @param line the line
     */
    public void addLine(Line line) {
        number2line.put(line.getNumber(), line);
    }

    /**
     * Adds a connection.
     * @param stations stations
     */
    public void addConnection(List<Station> stations) {
        for (Station station : stations) {
            if (!connections.containsKey(station)) {
                connections.put(station, new TreeSet<>());
            }
            TreeSet<Station> connectedStations = connections.get(station);
            connectedStations.addAll(stations.stream()
                    .filter(s -> !s.equals(station)).collect(Collectors.toList()));
        }
    }

    /**
     * Returns the line with the corresponding number.
     * @param number number.
     * @return line
     */
    public Line getLine(int number) {
        return number2line.get(number);
    }

    /**
     * Returns the station with the matching name.
     * @param name the name
     * @return the station
     */
    public Station getStation(String name) {
        for (Station station : stations) {
            if (station.getName().equalsIgnoreCase(name)) {
                return station;
            }
        }
        return null;
    }

    /**
     * Returns the station with the corresponding name and line number.
     * @param name the name
     * @param lineNumber line number
     * @return the station
     */
    public Station getStation(String name, int lineNumber) {
        Station query = new Station(name, getLine(lineNumber));
        Station station = stations.ceiling(query);
        return station.equals(query) ? station : null;
    }

    /**
     * Returns the set of stations that have a transition to the corresponding station.
     * @param station the station.
     * @return set of stations.
     */
    public Set<Station> getConnectedStations(Station station) {
        return connections.containsKey(station) ?
                connections.get(station) : new TreeSet<>();
    }
}
