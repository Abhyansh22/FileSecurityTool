/**
 * Interface to receive progress updates during cryptographic file operations.
 */
@FunctionalInterface
public interface ProgressListener {
    /**
     * Called when the operation progress updates.
     * 
     * @param percentage the current progress percentage (0 to 100)
     */
    void onProgress(int percentage);
}
