/**
 * 
 */
package net.sf.wubiq.clients;

import java.util.ArrayList;
import java.util.List;

import net.sf.wubiq.enums.AddressStatus;
import net.sf.wubiq.utils.InstallerBundle;
import net.sf.wubiq.utils.InstallerUtils;

/**
 * Represents a set of possible server connection addresses.
 * @author Federico Alcantara
 *
 */
public class AddressesTableModel extends BaseTableModel {
	private static final long serialVersionUID = 1L;
	
	private class RowValue {
		private String address;
		private AddressStatus status;
		private RowValue() {
			address = "";
			status = AddressStatus.NOT_TESTED;
		}
	}
	
	private List<RowValue> rowValues;
	
	public AddressesTableModel(String labelPrefix) {
		super(labelPrefix);
	}
	
	@Override
	public void addRow(Object[] rowData) {
		RowValue rowValue = new RowValue();
		if (rowData.length > 0) {
			rowValue.address = (String) rowData[0];
			if (rowData.length >= 2) {
				rowValue.status = (AddressStatus) rowData[1];
			}
		}
		getRowValues().add(rowValue);
		fireTableDataChanged();
	}
	
	@Override
	public void removeRow(int row) {
		getRowValues().remove(row);
		fireTableRowsDeleted(row, row);
	}
	
	/**
	 * @see net.sf.wubiq.clients.BaseTableModel#removeAll()
	 */
	public void removeAll() {
		getRowValues().clear();
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return (column == 0);
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		RowValue rowValue = getRowValues().get(row);
		return column == 0 ? rowValue.address : 
			InstallerBundle.getLabel("AddressStatus." + rowValue.status.name());
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		RowValue rowValue = getRowValues().get(row);
		if (column == 0) {
			rowValue.address = (String)aValue;
		} else {
			rowValue.status = (AddressStatus)aValue;
		}
		fireTableCellUpdated(row, column);
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		return getRowValues().size();
	}
	
	/**
	 * @return Row values.
	 */
	private List<RowValue> getRowValues() {
		if (rowValues == null) {
			rowValues = new ArrayList<RowValue>();
		}
		return rowValues;
	}
	
	/**
	 * @see net.sf.wubiq.clients.BaseTableModel#cleanText(java.lang.String)
	 */
	public String cleanText(String sentText) {
		return InstallerUtils.INSTANCE.cleanInternetAddress(sentText);
	}

}
