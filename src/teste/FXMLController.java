/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste;

import java.awt.Event;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXButton;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;

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
	private Map<Integer, Integer> cow = new HashMap<>();
	private Map<Integer, Integer> pasture = new HashMap<>();
	private Image image;
	private BufferedImage bufferedImage;
	private boolean veri = true;
	private Thread thread;
	Runnable runnable;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		buttonSelectImage.setOnAction((final ActionEvent e) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JPG", "*.JPG"),
					new FileChooser.ExtensionFilter("PNG", "*.png"));

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
		/*
		 * buttonSelectPasture.setOnMouseClicked((event) -> { Bounds selectionBounds =
		 * rubberBandSelection.getBounds(); BufferedImage crop = crop(selectionBounds);
		 * rubberBandSelection.removeRetangle(); pasture = savePixelsInHashMap(crop);
		 * 
		 * });
		 */

		buttonSelectPasture.setOnMouseClicked((event) -> {
			Image imageCursor = new Image(getClass().getResourceAsStream("square.png")); // pass in the image path

			ImageCursor imageCursor2 = new ImageCursor(imageCursor, -100, -100);
			group.setCursor(imageCursor2);
			group.setOnMousePressed((event1) -> {
				veri = true;
				erasePixels(event1);
				thread.start();
			});
			group.setOnMouseReleased((event1) -> {
				veri = false;
				thread.interrupt();
			});

		});
		buttonSelectCow.setOnMouseClicked((event) -> {
			rubberBandSelection = new RubberBandSelection(group);
			Bounds selectionBounds = rubberBandSelection.getBounds();
			BufferedImage crop = crop(selectionBounds);
			rubberBandSelection.removeRetangle();
			cow = savePixelsInHashMap(crop);
		});
		buttonSelectCow.setDisable(true);
		buttonSelectStart.setOnMouseClicked((event) -> {

			/*
			 * File f = new File("cow1.txt"); //pega do arquivo e passa na imagem retirando
			 * cores try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			 * 
			 * String sCurrentLine;
			 * 
			 * while ((sCurrentLine = br.readLine()) != null) {
			 * cow.put(Integer.valueOf(sCurrentLine), Integer.valueOf(sCurrentLine)); }
			 * 
			 * pasture.forEach((t, u) -> { if (cow.get(u) != null) { cow.remove(u); } });
			 * File fi = new File("cow2.txt"); fi.createNewFile(); final PrintWriter out =
			 * new PrintWriter(new BufferedWriter(new FileWriter(fi, true)));
			 * cow.forEach((t, u) -> { out.println(cow.get(u)); }); } catch (IOException e)
			 * { }
			 */

			File f = new File("cow2.txt"); // pega do arquivo e passa na imagem retirando cores
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {

				String sCurrentLine;

				while ((sCurrentLine = br.readLine()) != null) {
					cow.put(Integer.valueOf(sCurrentLine), Integer.valueOf(sCurrentLine));
				}
				replacePixels(SwingFXUtils.fromFXImage(imageView.getImage(), null));

			} catch (IOException e) {
				e.printStackTrace();
			}

			/*
			 * try { // salva as cores da vaca em um arquvio File f = new File("test.txt");
			 * f.createNewFile(); final PrintWriter out = new PrintWriter(new
			 * BufferedWriter(new FileWriter(f, true))); if (pasture == null || cow == null
			 * || pasture.size() == 1 || cow.size() == 1) { return; } pasture.forEach((t, u)
			 * -> { if(cow.get(u)!= null)cow.remove(u); }); cow.forEach((t,u)->{
			 * out.println(cow.get(u)); }); out.close(); try { replacePixels(bufferedImage);
			 * } catch (IOException ex) {
			 * Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
			 * } } catch (IOException ex) {
			 * Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
			 * }
			 */
		});

	}

	private void erasePixels(MouseEvent event) {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				runnable = new Runnable() {
					@Override
					public void run() {
						int ct = 200; // tamanho da borracha
						Double x = event.getX();
						Double y = event.getY();
						BufferedImage fromFXImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
						for (int i = 0; i < ct; i++) {
							fromFXImage.setRGB(x.intValue() + i, y.intValue(), -1);
							for (int j = 0; j < ct; j++) {
								fromFXImage.setRGB(x.intValue() + i, y.intValue() + j, -1);
							}
						}
						image = SwingFXUtils.toFXImage(fromFXImage, null);
						imageView.setImage(image);
						imageView.setFitWidth(image.getWidth());
						imageView.setFitHeight(image.getHeight());

					}
				};
				Platform.runLater(runnable);
			}
		});
	}

	public BufferedImage replacePixels(BufferedImage crop) throws IOException {

		int w = crop.getWidth();
		int h = crop.getHeight();
		BufferedImage writableImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		boolean[][] matriz = new boolean[h][w];
		for (int lin = 0; lin < h; lin++) {
			for (int col = 0; col < w; col++) {
				int pixel = crop.getRGB(col, lin);
				if (cow.get(pixel) != null) {
					writableImage.setRGB(col, lin, -16777216);
					matriz[lin][col] = true;
				} else {
					writableImage.setRGB(col, lin, -1);
					matriz[lin][col] = false;
				}
			}
		}

		// ImageJ fazendo o tratamento da img
		ImagePlus binaerpic = new ImagePlus();
		binaerpic.setImage(writableImage);
		binaerpic.unlock();
		binaerpic.setProcessor(binaerpic.getTitle(), binaerpic.getProcessor().convertToByte(false));
		ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_OUTLINES, 0, null, 1600, 50000);
		Prefs.blackBackground = false;
		pa.analyze(binaerpic);
		pa.setHideOutputImage(true);
		writableImage = pa.getOutputImage().getBufferedImage();

		image = SwingFXUtils.toFXImage(writableImage, null);
		imageView.setImage(image);
		imageView.setFitWidth(image.getWidth());
		imageView.setFitHeight(image.getHeight());
		File outputfile = new File("image.jpg");
		ImageIO.write(writableImage, "jpg", outputfile);
		// quantidade de pixels pretos encontrados apos o imagej tratar a img
		int pix = 0;
		for (int lin = 0; lin < h; lin++) {
			for (int col = 0; col < w; col++) {
				int pixel = writableImage.getRGB(col, lin);
				if (pixel != -1)
					pix++;

			}
		}
		System.out.println(pix / 1750);
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

		BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
		BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),
				BufferedImage.OPAQUE);

		Graphics2D graphics = bufImageRGB.createGraphics();
		graphics.drawImage(bufImageARGB, 0, 0, null);

		graphics.dispose();

		return bufImageRGB;
	}

}
