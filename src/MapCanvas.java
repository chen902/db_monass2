import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

import java.sql.*;

public class MapCanvas extends Canvas
{
    private final int PADDING = 50;
    private final int ICON_RADIUS = 10;
    private GraphicsContext gc = null;
    private Connection conn = null;
    private PreparedStatement cities = null;
    private PreparedStatement roads = null;
    private ResultSet rs = null;
    private double width;
    private double height;

    public MapCanvas()
    {
        super();

        // initialize connection and prepared statements
        this.initializeConn();
        this.setWidth(this.width+PADDING);
        this.setHeight(this.height+PADDING);
        this.setPreparedStatements();
        // Get the graphics context for the canvas & clear.
        gc = getGraphicsContext2D();

        clear();
        String instruction = "Please select a map to the left";
        double x = (this.getWidth()/2);
        gc.strokeText("Please select a map to the left", x,this.getHeight()/2);
    }

    public void show(String mapName)
    {
        this.clear();

        try
        {
            // Draw all roads as lines from each two cities
            this.roads.setString(1,mapName);
            this.rs = this.roads.executeQuery();
            while(this.rs.next())
            {
                double origX = this.rs.getDouble(1);
                double origY = this.rs.getDouble(2);
                double destX = this.rs.getDouble(3);
                double destY = this.rs.getDouble(4);
                int distance = this.rs.getInt(5);

                this.gc.strokeLine(origX,origY,destX,destY);
                double distanceX = (origX+destX)/2;
                double distanceY = (origY+destY)/2;
                this.gc.strokeText(String.valueOf(distance),distanceX,distanceY);
            }

            // Draw all cities for the current map name
            this.cities.setString(1,mapName);
            this.rs = this.cities.executeQuery();
            while(this.rs.next())
            {
                String name = this.rs.getString(1);
                double x = this.rs.getDouble(2);
                double y = this.rs.getDouble(3);

                // center oval on coordinate
                this.gc.fillOval(x-ICON_RADIUS/2,y-ICON_RADIUS/2,ICON_RADIUS,ICON_RADIUS);

                // offset by 10 pixels and write city name
                this.gc.strokeText(name, x+10,y+10);
            }
        }
        catch(SQLException e)
        {
            new Alert(Alert.AlertType.ERROR,"Error getting map information from database!").showAndWait();
        }
    }

    public void clear()
    {
        // clear canvas by drawing a gray rectangle across is
        gc.clearRect(0, 0, this.gc.getCanvas().getWidth(), this.gc.getCanvas().getHeight());
        gc.setStroke(Color.LIGHTSLATEGRAY);
        gc.strokeRect(0, 0, this.gc.getCanvas().getWidth(), this.gc.getCanvas().getHeight());
    }

    private void initializeConn()
    {
        try
        {

            // initiate connection to database
            this.conn = DriverManager.getConnection(Main.CONN_STRING);

            // get maximal coordinates to set the canvas size accordingly
            Statement s = this.conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT MAX(PosX), MAX(PosY) FROM CITY");
            rs.next();
            this.width = rs.getDouble(1);
            this.height = rs.getDouble(2);

        } catch(SQLException e)
        {
            // in case of an error show error message and quit the application
            new Alert(Alert.AlertType.ERROR,
                      "Error initializing connection to the database!",
                      ButtonType.CLOSE)
                    .showAndWait();
            Platform.exit();
        }
    }

    private void setPreparedStatements()
    {
        try
        {
            // query all cities in a map with the city coordinates
            this.cities = this.conn.prepareStatement("SELECT C.Name,C.PosX,C.PosY " +
                                                     "FROM CITY C " +
                                                     "INNER JOIN MAP M ON M.ID = C.MapID " +
                                                     "WHERE M.Name = ?");

            // query all roads in a map with the coordinates of source and destination city
            this.roads = this.conn.prepareStatement("SELECT C1.PosX,C1.PosY, C2.PosX,C2.PosY,R.Distance " +
                                                    "FROM ROAD R, CITY C1, CITY C2 " +
                                                    "WHERE R.IDfrom = C1.ID AND R.IDto = C2.ID " +
                                                        "AND R.MapID = (SELECT ID FROM MAP WHERE Name=?)");

        }
        catch(SQLException e)
        {
            new Alert(Alert.AlertType.ERROR,"Error forming prepared statements!", ButtonType.CLOSE).showAndWait();
            Platform.exit();
        }
    }

    // closes all prepared statements and the connection to the database
    public void destroy()
    {
        try
        {
            if(this.cities != null)
                this.cities.close();

            if(this.roads != null)
                this.roads.close();

            if(this.conn != null)
            {
                this.conn.close();
            }
        }
        catch(SQLException e)
        {
            // we tried
        }
    }

}
