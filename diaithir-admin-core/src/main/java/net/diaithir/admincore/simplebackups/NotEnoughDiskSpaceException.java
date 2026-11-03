// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: moved into the feature package.
package net.diaithir.admincore.simplebackups;

import java.io.IOException;
import java.io.Serial;

public class NotEnoughDiskSpaceException extends IOException {
    @Serial
    private static final long serialVersionUID = 20251031L;

    public NotEnoughDiskSpaceException(String message) {
        super(message);
    }
}
