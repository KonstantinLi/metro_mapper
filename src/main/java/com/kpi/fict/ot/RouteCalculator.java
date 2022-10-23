package com.kpi.fict.ot;

import com.kpi.fict.ot.core.Station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The class contains the underlying business logic for computing processes.
 * @version 1.0-SNAPSHOT
 * @author Linenko Kostyantyn, student of IO-01 in KPI
 */
public class RouteCalculator {
    /** Data container */
    private final StationIndex stationIndex;

    /** Average travel time between neighboring stations <b>without</b> a transfer. */
    private static final double INTER_STATION_DURATION = 2.5;
    /** Average travel time between neighboring stations <b>with</b> a transfer. */
    private static final double INTER_CONNECTION_DURATION = 3.5;

    /**
     * A single container.
     * @param stationIndex station index.
     */
    public RouteCalculator(StationIndex stationIndex) {
        this.stationIndex = stationIndex;
    }

    /**
     * Finds the shortest route. A trip with a minimum number of transfers has an advantage over other options.
     * <b>Attention</b>: the shortest path does not always correspond to the route with the shortest travel time. It is related
     * with no data on the exact travel time between specific stations. This problem should be solved in subsequent versions.
     * @param from the station of departure
     * @param to destination station
     * @return the shortest route
     */
    // TODO: Необхідно враховувати тривалість маршруту між станціями.
    public List<Station> getShortestRoute(Station from, Station to) {
        List<Station> route = getRouteOnTheLine(from, to);
        if (route != null) {
            return route;
        }

        route = getRouteWithOneConnection(from, to);
        if (route != null) {
            return route;
        }

        route = getRouteWithTwoConnections(from, to);
        return route;
    }

    /**
     * Calculates the approximate travel time.
     * @param route list of stations
     * @return time
     */
    public static double calculateDuration(List<Station> route) {
        double duration = 0;
        Station previousStation = null;
        for (int i = 0; i < route.size(); i++) {
            Station station = route.get(i);
            if (i > 0) {
                duration += previousStation.getLine().equals(station.getLine()) ?
                        INTER_STATION_DURATION : INTER_CONNECTION_DURATION;
            }
            previousStation = station;
        }
        return duration;
    }

    /**
     * Calculates the route on one line.
     * Iterates over all stations of the corresponding line. If the desired station (departure or destination) is found in the list,
     * then the variable direction is assigned the value 1 or -1, respectively. This is necessary in order to determine the direction of tracking
     * stations and, if necessary, return the reverse route.
     * If the stations are not on the same line, the method returns <b>null</b>.
     * @param from the station of departure
     * @param to destination station
     * @return route or null
     */
    private List<Station> getRouteOnTheLine(Station from, Station to) {
        if (!from.getLine().equals(to.getLine())) {
            return null;
        }
        List<Station> route = new ArrayList<>();
        List<Station> stations = from.getLine().getStations();
        int direction = 0;
        for (Station station : stations) {
            if (direction == 0) {
                if (station.equals(from)) {
                    direction = 1;
                } else if (station.equals(to)) {
                    direction = -1;
                }
            }

            if (direction != 0) {
                route.add(station);
            }

            if ((direction == 1 && station.equals(to)) ||
                    (direction == -1 && station.equals(from))) {
                break;
            }
        }
        if (direction == -1) {
            Collections.reverse(route);
        }
        return route;
    }


    /**
     * Calculates a route with one stop. In the cycle, it searches for a pair of stations that have a connection, splits the route into two
     * parts and separately calculates the route along one line and combines the results into a single list.
     * If the stations are on the same line, the method returns <b>null</b>.
     * @param from the station of departure
     * @param to destination station
     * @return route or null
     */
    private List<Station> getRouteWithOneConnection(Station from, Station to) {
        if (from.getLine().equals(to.getLine())) {
            return null;
        }

        List<Station> route = new ArrayList<>();

        List<Station> fromLineStations = from.getLine().getStations();
        List<Station> toLineStations = to.getLine().getStations();
        for (Station srcStation : fromLineStations) {
            for (Station dstStation : toLineStations) {
                if (isConnected(srcStation, dstStation)) {
                    ArrayList<Station> way = new ArrayList<>();
                    way.addAll(getRouteOnTheLine(from, srcStation));
                    way.addAll(getRouteOnTheLine(dstStation, to));
                    if (route.isEmpty() || route.size() > way.size()) {
                        route.clear();
                        route.addAll(way);
                    }
                }
            }
        }
        return route.size() != 0 ? route : null; // исправлено здесь
    }

    /**
     * Checking if stations have transition.
     * @param station1 station-1
     * @param station2 station-2
     * @return true or false
     */
    private boolean isConnected(Station station1, Station station2) {
        Set<Station> connected = stationIndex.getConnectedStations(station1);
        return connected.contains(station2);
    }

    /**
     * Calculates the connecting line route.
     * @param from the station of departure
     * @param to destination station
     * @return route
     */
    private List<Station> getRouteViaConnectedLine(Station from, Station to) {
        Set<Station> fromConnected = stationIndex.getConnectedStations(from);
        Set<Station> toConnected = stationIndex.getConnectedStations(to);
        for (Station srcStation : fromConnected) {
            for (Station dstStation : toConnected) {
                if (srcStation.getLine().equals(dstStation.getLine())) {
                    return getRouteOnTheLine(srcStation, dstStation);
                }
            }
        }
        return null;
    }

    /**
     * Calculates a route with two transitions. Finds an intermediate route between some stations,
     * the trip between which is carried out with one transfer. Thus, the wanted is divided into 3 parts.
     * If the stations are on the same line, the method returns <b>null</b>.
     * @param from the station of departure
     * @param to destination station
     * @return route
     */
    private List<Station> getRouteWithTwoConnections(Station from, Station to) {
        if (from.getLine().equals(to.getLine())) {
            return null;
        }

        ArrayList<Station> route = new ArrayList<>();

        List<Station> fromLineStations = from.getLine().getStations();
        List<Station> toLineStations = to.getLine().getStations();

        for (Station srcStation : fromLineStations) {
            for (Station dstStation : toLineStations) {
                List<Station> connectedLineRoute =
                        getRouteViaConnectedLine(srcStation, dstStation);
                if (connectedLineRoute == null) {
                    continue;
                }
                List<Station> way = new ArrayList<>();
                way.addAll(getRouteOnTheLine(from, srcStation));
                way.addAll(connectedLineRoute);
                way.addAll(getRouteOnTheLine(dstStation, to));
                if (route.isEmpty() || route.size() > way.size()) {
                    route.clear();
                    route.addAll(way);
                }
            }
        }

        return route;
    }
}