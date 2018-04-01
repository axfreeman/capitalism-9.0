package capitalism.view.custom;

import capitalism.model.TimeStamp;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeItem;

public class TimeStampViewItem extends TreeItem<TimeStamp> {

    ChangeListener<Boolean> expandedListener = (obs, wasExpanded, isNowExpanded) -> {
        if (isNowExpanded) {
            ReadOnlyProperty<?> expandedProperty = (ReadOnlyProperty<?>) obs ;
            Object itemThatWasJustExpanded = expandedProperty.getBean();
            if(getParent()!=null) {
                for (TreeItem<TimeStamp> item : getParent().getChildren()) {
                    if (item != itemThatWasJustExpanded) {
                        item.setExpanded(false);
                    }
                }
            }
        }
    };
	public TimeStampViewItem(TimeStamp t) {
		super(t);
		setExpanded(true);// default is expanded
		
//		the line below, if used, closes all treeView branches except the one being viewed.		
//		expandedProperty().addListener(expandedListener);
	}
}
