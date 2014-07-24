/**
 * 
 */
package net.sf.wubiq.clients;

import java.util.ArrayList;
import java.util.List;

import net.sf.wubiq.utils.InstallerUtils;
import net.sf.wubiq.utils.Is;

/**
 * Represents a set of possible groups.
 * @author Federico Alcantara
 *
 */
public class TextStringTableModel extends BaseTableModel {
	private static final long serialVersionUID = 1L;
	private List<String> rowValues;
	
	public TextStringTableModel(String labelPrefix) {
		super(labelPrefix);
	}
	
	@Override
	public void addRow(Object[] rowDatas) {
		if (rowDatas.length > 0) {
			String rowData = (String)rowDatas[0];
			if (!Is.emptyString(rowData)) {
				getRowValues().add(rowData);
			}
		}
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
		String rowValue = getRowValues().get(row);
		return rowValue; 
	}
	
	/**
	 * Updates a value and makes sure that it is not duplicated nor blank.
	 * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public synchronized void setValueAt(Object aValue, int row, int column) {
		String toSaveValue = InstallerUtils.INSTANCE.cleanString((String)aValue);
		getRowValues().set(row, toSaveValue);
		List<String> newRows = new ArrayList<String>();
		for (int index = 0; index < getRowValues().size(); index++) {
			String value = getRowValues().get(index);
			if (!Is.emptyString(value) &&
					!newRows.contains(value)) {
				newRows.add(value);
			}
		}
		if (newRows.size() != getRowValues().size()) {
			rowValues = newRows;
			fireTableDataChanged();
		} else {
			fireTableCellUpdated(row, column);
		}
	}
	
	@Override
	public int getColumnCount() {
		return 1;
	}
	
	@Override
	public int getRowCount() {
		return getRowValues().size();
	}
	
	/**
	 * @return Row values.
	 */
	private List<String> getRowValues() {
		if (rowValues == null) {
			rowValues = new ArrayList<String>();
		}
		return rowValues;
	}
	
	/**
	 * @see net.sf.wubiq.clients.BaseTableModel#cleanText(java.lang.String)
	 */
	public String cleanText(String sentText) {
		return InstallerUtils.INSTANCE.cleanString(sentText);
	}
}
