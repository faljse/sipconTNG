/** IMtext class used for the IMpreview mode in Sipcon1
 *
 */
package se.omnitor.protocol.t140;

public class IMtext {
	public String entryText;
	public int entrySize;
	public IMtext prevEntry = null;
	public IMtext nextEntry = null;
	private String who;
	//id=1 is the transmitter
	//id=0 is the receiver
	//id=2 is reserved
	public int id;

	/* Constructor */
	public IMtext(int pid, String whoname) {
		id = pid;
		entryText = "";
		if (pid == 2) who = "";
		if (pid == 1) who = whoname;
		if (pid == 0) who = "<me> ";
	}

	/**
	 * Add text to an IMtext object
	 */

	public void addText(String textentry) {
		entryText = textentry;
		entrySize = textentry.length();
	}

	/**
	 * Recurse through all the IMtext objects to calculate the total text size
	 */

	public int getTextSize() {
		if (prevEntry == null)
			return entrySize;
		else
			return (entrySize + prevEntry.getTextSize());
	}

	/**
	 * Recurse through all the IMtext objects to get the
	 * total text string to present in the gui
	 */

	public String getLog() {
		if (prevEntry == null)
			return who + entryText;
		else
			return prevEntry.getLog() + "\n" + who + entryText;
	}

	/**
	 * When erasing the last character the IMtext objects need
	 * to relink the message chain and return the text in the
	 * unlinked object.
	 */
	public String getPrevEntryText(int pid) {
		if (prevEntry == null) {
			if (id == pid) {
				nextEntry.prevEntry = null;
				return entryText;
			} else {
				return ""; //No previous IMtext object
			}
		} else
			if (id == pid) {
				/* Relink the previous relation to get the last
				 * message from either the receiver or transmitter
				 */
				nextEntry.prevEntry = this.prevEntry;
				prevEntry.nextEntry = this.nextEntry;
				return entryText;
			} else
				return prevEntry.getPrevEntryText(pid);
	}

	/**
	 * Insert an IMtext object after the anchor IMtext object
	 */

	public void insertIMtextObject(IMtext newObject) {
		if (prevEntry == null) {
			prevEntry = newObject;
			newObject.nextEntry = this;
		} else {
			newObject.nextEntry = this;
			newObject.prevEntry = prevEntry;
			prevEntry.nextEntry = newObject;
			prevEntry = newObject;
		}
	}

}
