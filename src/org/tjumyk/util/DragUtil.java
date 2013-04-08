package org.tjumyk.util;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DragUtil {
	protected static double initX, initY;
	protected static double lastX, lastY;
	protected static long lastTime;
	protected static double speedX, speedY;
	protected static Point2D dragAnchor;
	protected static Timeline slideTimeline;
	public static double MAX_SPEED = 10;
	protected static Interpolator interplator = new Interpolator() {

		@Override
		protected double curve(double t) {
			return Math.sin(t * Math.PI / 2);
		}
	};

	public static void setDraggable(final Node node) {
		setDraggable(node, true, true);
	}

	public static void setXDraggable(final Node node) {
		setDraggable(node, true, false);
	}

	public static void setYDraggable(final Node node) {
		setDraggable(node, false, true);
	}

	public static void setDraggable(final Node node, final boolean allowDragX,
			final boolean allowDragY) {
		setDraggable(node, node, allowDragX, allowDragY);
	}

	public static void setDraggable(final Node listenNode, final Node dragNode) {
		setDraggable(listenNode, dragNode, true, true);
	}

	public static void setXDraggable(final Node listenNode, final Node dragNode) {
		setDraggable(listenNode, dragNode, true, false);
	}

	public static void setYDraggable(final Node listenNode, final Node dragNode) {
		setDraggable(listenNode, dragNode, false, true);
	}

	public static void setDraggable(final Node listenNode, final Node dragNode,
			final boolean allowDragX, final boolean allowDragY) {
		setDraggable(listenNode, dragNode, allowDragX, allowDragY, 0, null);
	}

	public static void setDraggable(final Node listenNode, final Node dragNode,
			final boolean allowDragX, final boolean allowDragY,
			final double sensitivity, final DragLimitArea limitRect) {
		listenNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				double dragX = me.getScreenX() - dragAnchor.getX();
				double dragY = me.getScreenY() - dragAnchor.getY();
				long deltaT = System.currentTimeMillis() - lastTime;
				if (allowDragX)
					speedX = (me.getSceneX() - lastX) / deltaT;
				if (allowDragY)
					speedY = (me.getSceneY() - lastY) / deltaT;
				lastX = me.getSceneX();
				lastY = me.getSceneY();
				lastTime = System.currentTimeMillis();
				double newXPosition = initX + dragX;
				double newYPosition = initY + dragY;
				if (allowDragX)
					dragNode.setLayoutX(newXPosition);
				if (allowDragY)
					dragNode.setLayoutY(newYPosition);
				me.consume();
			}
		});
		listenNode.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				double speed = Math.sqrt(Math.pow(speedX, 2)
						+ Math.pow(speedY, 2));
				if (speed == 0 || sensitivity <= 0)
					return;
				if(speed > MAX_SPEED)
					speed = MAX_SPEED;
				double deltaT = sensitivity * speed / 50;
				double deltaD = sensitivity * Math.pow(speed, 2) * 5;
				double deltaX = deltaD * speedX / speed;
				double deltaY = deltaD * speedY / speed;

				double newLayoutX = dragNode.getLayoutX() + deltaX;
				double newLayoutY = dragNode.getLayoutY() + deltaY;
				if (limitRect != null) {
					if (newLayoutX < limitRect.getMinX())
						newLayoutX = limitRect.getMinX();
					if (newLayoutX > limitRect.getMaxX())
						newLayoutX = limitRect.getMaxX();
					if (newLayoutY < limitRect.getMinY())
						newLayoutY = limitRect.getMinY();
					if (newLayoutY > limitRect.getMaxY())
						newLayoutY = limitRect.getMaxY();
				}

				if (slideTimeline != null)
					slideTimeline.stop();
				slideTimeline = new Timeline(//
						new KeyFrame(Duration.seconds(deltaT), //
								new KeyValue(dragNode.layoutXProperty(),
										newLayoutX, interplator),//
								new KeyValue(dragNode.layoutYProperty(),
										newLayoutY, interplator)//
						));
				slideTimeline.play();
				me.consume();
			};
		});
		listenNode.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (slideTimeline != null)
					slideTimeline.stop();
				initX = dragNode.getLayoutX();
				initY = dragNode.getLayoutY();
				dragAnchor = new Point2D(lastX = me.getScreenX(), lastY = me
						.getScreenY());
				speedX = speedY = 0;
				lastTime = System.currentTimeMillis();
				dragNode.toFront();
				me.consume();
			}
		});
	}

	public static void setDraggable(final Stage stage) {
		stage.sceneProperty().addListener(new ChangeListener<Scene>() {

			@Override
			public void changed(ObservableValue<? extends Scene> observable,
					Scene oldValue, Scene newScene) {
				final Scene scene = newScene;
				newScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent me) {
						double dragX = me.getScreenX() - dragAnchor.getX();
						double dragY = me.getScreenY() - dragAnchor.getY();
						double newXPosition = initX + dragX;
						double newYPosition = initY + dragY;
						scene.getWindow().setX(newXPosition);
						scene.getWindow().setY(newYPosition);
						me.consume();
					}
				});
				newScene.setOnMousePressed(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent me) {
						initX = scene.getWindow().getX();
						initY = scene.getWindow().getY();
						dragAnchor = new Point2D(me.getScreenX(), me
								.getScreenY());
					}
				});

			}

		});
	}

	public static void setDraggable(final Stage stage, final Node listenNode) {
		final Scene scene = stage.getScene();
		listenNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (stage.isFullScreen())
					return;
				double dragX = me.getScreenX() - dragAnchor.getX();
				double dragY = me.getScreenY() - dragAnchor.getY();
				double newXPosition = initX + dragX;
				double newYPosition = initY + dragY;
				scene.getWindow().setX(newXPosition);
				scene.getWindow().setY(newYPosition);
				me.consume();
			}
		});
		listenNode.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				if (stage.isFullScreen())
					return;
				initX = scene.getWindow().getX();
				initY = scene.getWindow().getY();
				dragAnchor = new Point2D(me.getScreenX(), me.getScreenY());
			}
		});
	}
	public static void setPaneClip(final Pane container) {
		container.clipProperty().bind(new ObjectBinding<Node>() {
			{
				bind(container.widthProperty(), container.heightProperty());
			}
			@Override
			protected Node computeValue() {
				return new Rectangle(container.getWidth(), container
						.getHeight());
			}
		});
	}
	public static class DragLimitArea {
		public double x, y, width, height;

		public DragLimitArea(double x, double y, double width, double height) {
			super();
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return "DragLimitArea [x=" + x + ", y=" + y + ", width=" + width
					+ ", height=" + height + "]";
		}

		public double getMinX() {
			return Math.min(x, x + width);
		}

		public double getMinY() {
			return Math.min(y, y + height);
		}

		public double getMaxX() {
			return Math.max(x, x + width);
		}

		public double getMaxY() {
			return Math.max(y, y + height);
		}
	}
}
