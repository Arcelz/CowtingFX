/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste;

import com.jfoenix.controls.JFXButton;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author Lucas
 */
public class FXMLController implements Initializable {

    @FXML
    private JFXButton buttonSelectImage;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ImageView imageView;

    @FXML
    private Group group;

    @FXML
    private JFXButton buttonSelectPasture;

    @FXML
    private JFXButton buttonSelectCow;

    @FXML
    private JFXButton buttonSelectStart;

    private RubberBandSelection rubberBandSelection;
    private Map cow, otherCow, pasture;
    private Image image;
    private BufferedImage bufferedImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buttonSelectImage.setOnAction((final ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"));

            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                try {
                    bufferedImage = ImageIO.read(file);
                    image = SwingFXUtils.toFXImage(bufferedImage, null);
                    imageView.setImage(image);
                    imageView.setFitWidth(image.getWidth());
                    imageView.setFitHeight(image.getHeight());
                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        rubberBandSelection = new RubberBandSelection(group);
        buttonSelectPasture.setOnMouseClicked((event) -> {
            Bounds selectionBounds = rubberBandSelection.getBounds();
            BufferedImage crop = crop(selectionBounds);
            rubberBandSelection.removeRetangle();
            pasture = savePixelsInHashMap(crop);

        });
        buttonSelectCow.setOnMouseClicked((event) -> {
            Bounds selectionBounds = rubberBandSelection.getBounds();
            BufferedImage crop = crop(selectionBounds);
            rubberBandSelection.removeRetangle();
            cow = savePixelsInHashMap(crop);
        });
        buttonSelectStart.setOnMouseClicked((event) -> {
            if (pasture == null || cow == null || pasture.size() == 1 || cow.size() == 1) {
                return;
            }
            
            try {
                replacePixels(bufferedImage);
            } catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

    }

    public BufferedImage replacePixels(BufferedImage crop) throws IOException {

        int w = crop.getWidth();
        int h = crop.getHeight();
  
        BufferedImage writableImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

         for (int col = 0; col < w; col++) {
            for (int lin = 0; lin < h; lin++) {
                int pixel = crop.getRGB(col, lin);
                if (pasture.get(pixel) != null) {
                    writableImage.setRGB(col, lin, -1);                 
                }
                else{
                writableImage.setRGB(col, lin, pixel);
                }
            }
        }

        image = SwingFXUtils.toFXImage(writableImage, null);
        imageView.setImage(image);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());

        return crop;
    }

    public Map<Integer, Integer> savePixelsInHashMap(BufferedImage crop) {
        Map<Integer, Integer> map = new HashMap<>();
int w = crop.getWidth();
        int h = crop.getHeight();
          for (int col = 0; col < w; col++) {
            for (int lin = 0; lin < h; lin++) {
                int pixel = crop.getRGB(col, lin);
                map.put(pixel, pixel);
            }
        }
        return map;
    }

    public static class RubberBandSelection {

        final DragContext dragContext = new DragContext();
        Rectangle rect = new Rectangle();

        Group group;

        public Bounds getBounds() {
            return rect.getBoundsInParent();
        }

        public RubberBandSelection(Group group) {

            this.group = group;

            rect = new Rectangle(0, 0, 0, 0);
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(1);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if (event.isSecondaryButtonDown()) {
                    return;
                }

                // remove old rect
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove(rect);

                // prepare new drag operation
                dragContext.mouseAnchorX = event.getX();
                dragContext.mouseAnchorY = event.getY();

                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().add(rect);

            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if (event.isSecondaryButtonDown()) {
                    return;
                }

                double offsetX = event.getX() - dragContext.mouseAnchorX;
                double offsetY = event.getY() - dragContext.mouseAnchorY;

                if (offsetX > 0) {
                    rect.setWidth(offsetX);
                } else {
                    rect.setX(event.getX());
                    rect.setWidth(dragContext.mouseAnchorX - rect.getX());
                }

                if (offsetY > 0) {
                    rect.setHeight(offsetY);
                } else {
                    rect.setY(event.getY());
                    rect.setHeight(dragContext.mouseAnchorY - rect.getY());
                }
            }
        };

        private void removeRetangle() {
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);
            group.getChildren().remove(rect);
        }

        private static final class DragContext {

            public double mouseAnchorX;
            public double mouseAnchorY;

        }
    }

    private BufferedImage crop(Bounds bounds) {

        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), width, height));

        WritableImage wi = new WritableImage(width, height);
        imageView.snapshot(parameters, wi);

        // save image 
        // !!! has bug because of transparency (use approach below) !!!
        // --------------------------------
//        try {
//          ImageIO.write(SwingFXUtils.fromFXImage( wi, null), "jpg", file);
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
        // save image (without alpha)
        // --------------------------------
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        graphics.dispose();

        return bufImageRGB;
    }

}
