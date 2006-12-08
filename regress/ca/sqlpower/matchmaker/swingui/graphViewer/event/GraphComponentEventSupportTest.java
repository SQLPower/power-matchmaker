package ca.sqlpower.matchmaker.swingui.graphViewer.event;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.swingui.graphViewer.DefaultEdge;
import ca.sqlpower.matchmaker.swingui.graphViewer.DefaultEdgeFactory;
import ca.sqlpower.matchmaker.swingui.graphViewer.DefaultNode;
import ca.sqlpower.matchmaker.swingui.graphViewer.Diedge;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphComponent;

public class GraphComponentEventSupportTest extends TestCase {

	public class GraphComponentCountingListener implements GraphComponentListener {

		int edgeCut;
		int edgeDirectionSwap;
		int newEdgeAdded;
		int propertyChange;
		GraphComponentEvent lastEvent;
		
		public GraphComponentEvent getLastEvent() {
			return lastEvent;
		}

		public void setLastEvent(GraphComponentEvent lastEvent) {
			this.lastEvent = lastEvent;
		}

		public void gcEdgeCut(GraphComponentEvent evt) {
			edgeCut++;
			lastEvent = evt;
		}

		public void gcEdgeDirectionSwap(GraphComponentEvent evt) {
			edgeDirectionSwap++;
			lastEvent = evt;
		}

		public void gcNewEdgeAddedToNode(GraphComponentEvent evt) {
			newEdgeAdded++;
			lastEvent = evt;
		}

		public void gcPropertyChanged(GraphComponentEvent evt) {
			propertyChange++;
			lastEvent = evt;
		}

		public int getEdgeCut() {
			return edgeCut;
		}

		public void setEdgeCut(int edgeCut) {
			this.edgeCut = edgeCut;
		}

		public int getEdgeDirectionSwap() {
			return edgeDirectionSwap;
		}

		public void setEdgeDirectionSwap(int edgeDirectionSwap) {
			this.edgeDirectionSwap = edgeDirectionSwap;
		}

		public int getNewEdgeAdded() {
			return newEdgeAdded;
		}

		public void setNewEdgeAdded(int newEdgeAdded) {
			this.newEdgeAdded = newEdgeAdded;
		}

		public int getPropertyChange() {
			return propertyChange;
		}

		public void setPropertyChange(int propertyChange) {
			this.propertyChange = propertyChange;
		}
		
		public int getAllEvents(){
			return propertyChange+ edgeCut+edgeDirectionSwap+newEdgeAdded;
		}
	}
	
	GraphComponentEventSupport eventSupport;
	GraphComponentCountingListener l;
	
	protected void setUp() throws Exception {
		eventSupport = new GraphComponentEventSupport(new GraphComponent(){
			public void addGraphComponentListener(GraphComponentListener l) {
				// TODO Auto-generated method stub
			}

			public Rectangle getBounds(Rectangle b) {
				// TODO Auto-generated method stub
				return null;
			}

			public Rectangle getBounds() {
				// TODO Auto-generated method stub
				return null;
			}

			public int getHeight() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getLabel() {
				// TODO Auto-generated method stub
				return null;
			}

			public Point getLocation() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getToolTip() {
				// TODO Auto-generated method stub
				return null;
			}

			public int getWidth() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getX() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getY() {
				// TODO Auto-generated method stub
				return 0;
			}

			public void paint(Graphics g, double zoom) {
				// TODO Auto-generated method stub
				
			}

			public void removeGraphComponentListener(GraphComponentListener l) {
				// TODO Auto-generated method stub
				
			}

			public void setBounds(int x, int i, int width, int height) {
				// TODO Auto-generated method stub
				
			}

			public void setLocation(int i, int j) {
				// TODO Auto-generated method stub
				
			}
		});
		l = new GraphComponentCountingListener();
		eventSupport.addGraphComponentListener(l);
	}
	
	public void testEdgeCutEvent(){
		eventSupport.fireEdgeCut();
		assertEquals("Wrong number of events fired",1,l.getAllEvents());
		assertEquals("Wrong type of events fired should have been an edge cut",1,l.getEdgeCut());
	}
	
	public void testEdgeDirectionSwapEvent(){
		eventSupport.fireEdgeDirectionSwap();
		assertEquals("Wrong number of events fired",1,l.getAllEvents());
		assertEquals("Wrong type of events fired should have been direction swap",1,l.getEdgeDirectionSwap());
	}
	
	public void testEdgeaddedToNodeEvent(){
		Diedge diedge = new DefaultEdge(new DefaultNode("n1",new DefaultEdgeFactory()),new DefaultNode("n1",new DefaultEdgeFactory()));
		eventSupport.fireNewEdgeAddedToNode(diedge);
		assertEquals("Wrong number of events fired",1,l.getAllEvents());
		assertEquals("Wrong type of events fired should have been edge added to node",1,l.getNewEdgeAdded());
		assertEquals("Incorrect value for the edge", diedge,l.getLastEvent().getEdge());
	}

	public void testPropertyChangeEvent(){
		eventSupport.firePropertyChange("name", "2", "1");
		assertEquals("Wrong number of events fired",1,l.getAllEvents());
		assertEquals("Wrong type of events fired",1,l.getPropertyChange());
		assertEquals("Wrong property value ","name",l.getLastEvent().getPropertyName());
		assertEquals("Wrong old value ","2",l.getLastEvent().getOldValue());
		assertEquals("Wrong new value ","1",l.getLastEvent().getNewValue());
		eventSupport.firePropertyChange("name", "1", "1");
		assertEquals("Wrong number of events fired, property change shouldn't fire events when the values don't change",1,l.getAllEvents());
	}
}
