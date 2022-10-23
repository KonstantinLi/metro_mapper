import com.kpi.fict.ot.RouteCalculator;
import com.kpi.fict.ot.StationIndex;
import com.kpi.fict.ot.core.Line;
import com.kpi.fict.ot.core.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteCalculatorTest {

    RouteCalculator calculator;
    StationIndex stationIndex;

    @BeforeEach
    @DisplayName("Structure of metro")
    void beforeEach() {
        stationIndex = new StationIndex();

        /*
        --------------------------------------------------------------------------------
        Init
         */
        Line line1 = new Line(1, "Красная");
        Line line2 = new Line(2, "Зелёная");
        Line line3 = new Line(3, "Синяя");
        Line line4 = new Line(4, "Чёрная");

        Station station1 = new Station("Вокзальная", line1);
        Station station2 = new Station("Университет", line1);
        Station station3 = new Station("Театральная", line1);
        Station station4 = new Station("Нивки", line2);
        Station station5 = new Station("Славутич", line2);
        Station station6 = new Station("Бориспольская", line2);
        Station station7 = new Station("Лукьяновская", line2);
        Station station8 = new Station("Оболонь", line3);
        Station station9 = new Station("Лыбидская", line3);
        Station station10 = new Station("Васильковская", line3);
        Station station11 = new Station("Гидропарк", line4);
        Station station12 = new Station("Дарницкая", line4);
        Station station13 = new Station("Левобережная", line4);

        List<Station> connection1 = new ArrayList<>(Arrays.asList(station3, station6));
        List<Station> connection2 = new ArrayList<>(Arrays.asList(station2, station9));
        List<Station> connection3 = new ArrayList<>(Arrays.asList(station4, station8));
        List<Station> connection4 = new ArrayList<>(Arrays.asList(station1, station12));

        /*
        --------------------------------------------------------------------------------
        Record
         */
        List<Line> lines = new ArrayList<>(Arrays.asList(
                line1, line2, line3, line4
        ));

        List<Station> stationsOnRedLine = new ArrayList<>(Arrays.asList(
                station1, station2, station3
        ));
        List<Station> stationsOnGreenLine = new ArrayList<>(Arrays.asList(
                station4, station5, station6, station7
        ));
        List<Station> stationsOnBlueLine = new ArrayList<>(Arrays.asList(
                station8, station9, station10
        ));
        List<Station> stationsOnBlackLine = new ArrayList<>(Arrays.asList(
                station11, station12, station13
        ));

        List<List<Station>> connections = new ArrayList<>(Arrays.asList(
                connection1, connection2, connection3, connection4
        ));

        stationsOnRedLine.forEach(line1::addStation);
        stationsOnGreenLine.forEach(line2::addStation);
        stationsOnBlueLine.forEach(line3::addStation);
        stationsOnBlackLine.forEach(line4::addStation);

        lines.forEach(stationIndex::addLine);
        lines.stream().flatMap(line -> line.getStations().stream()).forEach(stationIndex::addStation);
        connections.forEach(stationIndex::addConnection);

        calculator = new RouteCalculator(stationIndex);
    }

    @Test
    @DisplayName("The shortest route on the line")
    void getShortestRouteOnTheLine() {
        Station from = stationIndex.getStation("Оболонь");
        Station to = stationIndex.getStation("Васильковская");

        List<Station> expectedRoute = new ArrayList<>(Arrays.asList(
                from, stationIndex.getStation("Лыбидская"), to
        ));

        assertEquals(expectedRoute, calculator.getShortestRoute(from, to));
    }

    @Test
    @DisplayName("The shortest route with one connection")
    void getShortestRouteWithOneConnection() {
        Station from = stationIndex.getStation("Университет");
        Station to = stationIndex.getStation("Васильковская");

        List<Station> expectedRoute = new ArrayList<>(Arrays.asList(
                from, stationIndex.getStation("Лыбидская"), to
        ));

        assertEquals(expectedRoute, calculator.getShortestRoute(from, to));
    }

    @Test
    @DisplayName("The shortest route with two connections")
    void getShortestRouteWithTwoConnections() {
        Station from = stationIndex.getStation("Васильковская");
        Station to = stationIndex.getStation("Гидропарк");

        List<Station> expectedRoute = new ArrayList<>(Arrays.asList(
                from, stationIndex.getStation("Лыбидская"),
                stationIndex.getStation("Университет"), stationIndex.getStation("Вокзальная"),
                stationIndex.getStation("Дарницкая"), to
        ));

        assertEquals(expectedRoute, calculator.getShortestRoute(from, to));
    }

    @Test
    @DisplayName("Travel duration evaluating")
    void calculateDuration() {
        List<Station> route = Arrays.asList(
                stationIndex.getStation("Оболонь"),
                stationIndex.getStation("Нивки"),
                stationIndex.getStation("Славутич"),
                stationIndex.getStation("Бориспольская")
        );

        double expectedDuration = 8.5;
        assertEquals(expectedDuration, RouteCalculator.calculateDuration(route));
    }
}