package de.unipaderborn.visuflow;

import java.util.List;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;

public enum ModelProvider {
        INSTANCE;

		private List<VFUnit> units;

        private ModelProvider() {
        	DataModel dataModel = ServiceUtil.getService(DataModel.class);
    		units = dataModel.listClasses().get(0).getMethods().get(2).getUnits();
        }

        public List<VFUnit> getUnits() {
                return units;
        }

}