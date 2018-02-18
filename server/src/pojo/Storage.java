package pojo;

public class Storage {
	int StorageID;
	int userID;
	String filename;
	int fragmentCount;
	int minFragment;
	
	public int getStorageID() {
		return StorageID;
	}
	public void setStorageID(int storageID) {
		StorageID = storageID;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getFragmentCount() {
		return fragmentCount;
	}
	public void setFragmentCount(int fragmentCount) {
		this.fragmentCount = fragmentCount;
	}
	public int getMinFragment() {
		return minFragment;
	}
	public void setMinFragment(int minFragment) {
		this.minFragment = minFragment;
	}
}
