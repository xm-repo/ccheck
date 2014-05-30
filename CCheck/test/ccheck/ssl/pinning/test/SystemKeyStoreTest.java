package ccheck.ssl.pinning.test;

import android.test.AndroidTestCase;
import ccheck.ssl.pinning.SystemKeyStore;

public class SystemKeyStoreTest extends AndroidTestCase {

	public void testConstruction() {
		assertNotNull(SystemKeyStore.getInstance(getContext()));
	}

}
