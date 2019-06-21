import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.sql.*;

public class MapSelectionPane extends VBox
{
    private static final int PANE_WIDTH = 10;
    private MapCanvas canvas;

    public MapSelectionPane(MapCanvas canvas)
    {
        super(PANE_WIDTH);
        this.canvas = canvas;

        ListView lw = new ListView();
        lw.setMinWidth(PANE_WIDTH);
        lw.setEditable(false);

        Connection conn = null;

        try
        {
            // initiate connection to database
            conn = DriverManager.getConnection(Main.CONN_STRING);
            Statement s = conn.createStatement();

            // query the names of all maps
            ResultSet rs = s.executeQuery("SELECT Name FROM MAP");

            // iterate on map names and add them to the list view
            while(rs.next())
            {
                lw.getItems().add(rs.getString(1));
            }
        }
        catch(SQLException e)
        {
            // in case of an error show error message and quit the application
            new Alert(Alert.AlertType.ERROR,
           "Error getting map details from database!",
                      ButtonType.CLOSE)
                    .showAndWait();
            Platform.exit();
        }
        finally
        {
            try
            {
                // attempt to close the connection in any case
                if(conn != null)
                    conn.close();
            }
            catch(SQLException e)
            {
                // nothing to do really
            }
        }

        lw.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                if(oldValue == null || ((String)oldValue).compareTo((String)newValue) != 0 )
                {
                    canvas.show(newValue.toString());
                }
            }
        });
        getChildren().addAll(lw);
    }
}
