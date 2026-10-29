package net.sf.wubiq.dotmatrix.witness;

import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of a {@link LayoutCheck}.
 * @author Federico Alcantara
 *
 */
public class CheckResult {
	public enum Status { PASS, FAIL, NOTE }

	public static class Item {
		private final Status status;
		private final String subject;
		private final String message;

		Item(Status status, String subject, String message) {
			this.status = status;
			this.subject = subject;
			this.message = message;
		}

		public Status getStatus() {
			return status;
		}

		public String getSubject() {
			return subject;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return status + "  " + subject + ": " + message;
		}
	}

	private final List<Item> items = new ArrayList<Item>();

	public void pass(String subject, String message) {
		items.add(new Item(Status.PASS, subject, message));
	}

	public void fail(String subject, String message) {
		items.add(new Item(Status.FAIL, subject, message));
	}

	public void note(String subject, String message) {
		items.add(new Item(Status.NOTE, subject, message));
	}

	public List<Item> getItems() {
		return items;
	}

	public int count(Status status) {
		int count = 0;
		for (Item item : items) {
			if (item.status == status) {
				count++;
			}
		}
		return count;
	}

	public boolean isPassed() {
		return count(Status.FAIL) == 0;
	}

	public String getSummary() {
		return (isPassed() ? "PASS" : "FAIL") + " (" + count(Status.PASS) + " ok, "
				+ count(Status.FAIL) + " failed, " + count(Status.NOTE) + " notes)";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getSummary()).append('\n');
		for (Status status : new Status[] {Status.FAIL, Status.NOTE, Status.PASS}) {
			for (Item item : items) {
				if (item.status == status) {
					builder.append(item).append('\n');
				}
			}
		}
		return builder.toString();
	}
}
