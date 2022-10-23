package com.kpi.fict.ot;

import com.kpi.fict.ot.core.Line;
import com.kpi.fict.ot.core.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The main class of the mini-project, the task of which is to calculate the shortest route between metro stations.
 * It is assumed that the program is used not only for a certain subway, but can be a reliable tool for every structure,
 * presented in any convenient format.
 * @see RouteCalculator
 * @see StationIndex
 * @version 1.0-SNAPSHOT
 * @author Linenko Kostyantyn, student of IO-01 in KPI
 */
public class Main {
    /**
     * The logger that is responsible for writing information to the logs based on the specified logging levels.
     */
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * Filters the history of valid inputs.
     */
    private static final Marker INPUT_HISTORY_MARKER = MarkerManager.getMarker("INPUT_HISTORY");
    /**
     * Filters input that cannot be used in the program.
     * These situations occur when the station does not exist.
     */
    private static final Marker INVALID_STATIONS_MARKER = MarkerManager.getMarker("INVALID_STATIONS");
    /**
     * Filters all possible exceptions.
     */
    private static final Marker EXCEPTIONS_MARKER = MarkerManager.getMarker("EXCEPTIONS");

    /**
     * The path to the file with the structure of the Kyiv metro.
     */
    private static final String DATA_FILE = "src/main/resources/kyiv.json";
    /**
     * Scanner.
     */
    private static Scanner scanner;

    /**
     * Structure container.
     */
    private static StationIndex stationIndex;

    /**
     * Application entry point.
     * There is an infinite loop for inputting stations and outputting the shortest route.
     */
    public static void main(String[] args) {
        RouteCalculator calculator = getRouteCalculator();

        System.out.println("Програма розрахунку маршрутів метрополітену Києва\n");
        scanner = new Scanner(System.in);
        for (; ; ) {
            Station from = takeStation("Введіть станцію відправлення:");
            Station to = takeStation("Введіть станцію призначення:");

            List<Station> route = calculator.getShortestRoute(from, to);
            System.out.println("Маршрут:");
            printRoute(route);
        }
    }

    /**
     * Returns a new {@link RouteCalculator} with a {@link StationIndex} object as a parameter.
     *
     * @return route calculator.
     */
    private static RouteCalculator getRouteCalculator() {
        createStationIndex();
        return new RouteCalculator(stationIndex);
    }

    /**
     * Outputs a route to the console.
     * <b>Note</b>: This method does not calculate the shortest route.
     * This is delegated to other methods.
     */
    private static void printRoute(List<Station> route) {
        Station previousStation = null;
        for (Station station : route) {
            if (previousStation != null) {
                Line prevLine = previousStation.getLine();
                Line nextLine = station.getLine();
                if (!prevLine.equals(nextLine)) {
                    System.out.println("\tПерехід на станцію " +
                            station.getName() + " (" + nextLine.getName() + " лінія)");
                }
            }
            System.out.println("\t" + station.getName());
            previousStation = station;
        }
    }

    /**
     * Finds a station by its name if it exists in the {@link StationIndex} object.
     * Otherwise, the warning will be sent to the log file.
     *
     * @param message the input string
     * @return {@link Station}
     */
    private static Station takeStation(String message) {
        for (; ; ) {
            System.out.println(message);
            String line = scanner.nextLine().trim();
            Station station = stationIndex.getStation(line);
            if (station != null) {
                LOGGER.info(INPUT_HISTORY_MARKER, "Введена станція '{}'", station.getName());
                return station;
            }
            LOGGER.warn(INVALID_STATIONS_MARKER, "Станція '{}' не знайдена", line);
            System.out.println("Станція не знайдена :(");
        }
    }

    /**
     * Generic method that provides parsing of various data represented in json.
     *
     * @see JSONParser
     * @see JSONObject
     * @see JSONArray
     */
    private static void createStationIndex() {
        stationIndex = new StationIndex();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(getJsonFile());

            JSONArray linesArray = (JSONArray) jsonData.get("lines");
            parseLines(linesArray);

            JSONObject stationsObject = (JSONObject) jsonData.get("stations");
            parseStations(stationsObject);

            JSONArray connectionsArray = (JSONArray) jsonData.get("connections");
            parseConnections(connectionsArray);
        } catch (Exception ex) {
            LOGGER.error(EXCEPTIONS_MARKER, "Parse exception", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Gets a list of stations with connections.
     *
     * @param connectionsArray JSON array
     */
    private static void parseConnections(JSONArray connectionsArray) {
        connectionsArray.forEach(connectionObject ->
        {
            JSONArray connection = (JSONArray) connectionObject;
            List<Station> connectionStations = new ArrayList<>();
            connection.forEach(item ->
            {
                JSONObject itemObject = (JSONObject) item;
                int lineNumber = ((Long) itemObject.get("line")).intValue();
                String stationName = (String) itemObject.get("station");

                Station station = stationIndex.getStation(stationName, lineNumber);
                if (station == null) {
                    throw new IllegalArgumentException("com.kpi.fict.ot.core.Station " +
                            stationName + " on line " + lineNumber + " not found");
                }
                connectionStations.add(station);
            });
            stationIndex.addConnection(connectionStations);
        });
    }

    /**
     * Receives stations.
     *
     * @param stationsObject JSON object
     */
    private static void parseStations(JSONObject stationsObject) {
        stationsObject.keySet().forEach(lineNumberObject ->
        {
            int lineNumber = Integer.parseInt((String) lineNumberObject);
            Line line = stationIndex.getLine(lineNumber);
            JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
            stationsArray.forEach(stationObject ->
            {
                Station station = new Station((String) stationObject, line);
                stationIndex.addStation(station);
                line.addStation(station);
            });
        });
    }

    /**
     * Gets lines.
     *
     * @param linesArray JSON array
     */
    private static void parseLines(JSONArray linesArray) {
        linesArray.forEach(lineObject -> {
            JSONObject lineJsonObject = (JSONObject) lineObject;
            Line line = new Line(
                    ((Long) lineJsonObject.get("number")).intValue(),
                    (String) lineJsonObject.get("name")
            );
            stationIndex.addLine(line);
        });
    }

    /**
     * Reads a json file and returns it as a string.
     *
     * @return string
     */
    private static String getJsonFile() {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(DATA_FILE));
            lines.forEach(builder::append);
        } catch (Exception ex) {
            LOGGER.error(EXCEPTIONS_MARKER, "File {} not found", DATA_FILE, ex);
            ex.printStackTrace();
        }
        return builder.toString();
    }
}