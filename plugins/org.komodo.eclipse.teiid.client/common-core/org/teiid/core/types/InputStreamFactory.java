/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.teiid.core.types;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLXML;
import javax.activation.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.xml.transform.Source;
import org.teiid.core.util.ReaderInputStream;

public abstract class InputStreamFactory implements Source {
	
	public enum StorageMode {
		MEMORY, //TODO: sources may return Serial values that are much too large and we should convert them to persistent.
		PERSISTENT,
		FREE,
		OTHER
	}
	
	public interface StreamFactoryReference {
		
		void setStreamFactory(InputStreamFactory inputStreamFactory);
		
	}
	
	private String systemId;
	protected long length = -1;
	
    /**
     * Get a new InputStream
     * @return
     */
    public abstract InputStream getInputStream() throws IOException;
    
    @Override
    public String getSystemId() {
    	return this.systemId;
    }
    
    @Override
    public void setSystemId(String systemId) {
    	this.systemId = systemId;
    }
    
    /**
	 * @throws IOException  
	 */
    public void free() throws IOException {
    	
    }
    
    /**
     * Length in bytes of the {@link InputStream}
     * @return the length or -1 if the length is not known
     */
    public long getLength() {
		return length;
	}
    
    public void setLength(long length) {
		this.length = length;
	}
    
    /**
	 * @throws IOException  
	 */
    public Reader getCharacterStream() throws IOException {
    	return null;
    }
    
    public StorageMode getStorageMode() {
    	return StorageMode.OTHER;
    }
    
    public static class FileInputStreamFactory extends InputStreamFactory {
    	
    	private File f;
    	
    	public FileInputStreamFactory(File f) {
    		this.f = f;
    		this.setSystemId(f.toURI().toASCIIString());
		}
    	
    	@Override
    	public long getLength() {
    		return f.length();
    	}
    	
    	@Override
    	public InputStream getInputStream() throws IOException {
    		return new BufferedInputStream(new FileInputStream(f));
    	}
    	
    	@Override
    	public StorageMode getStorageMode() {
    		return StorageMode.PERSISTENT;
    	}
    	
    }
    
    public static class ClobInputStreamFactory extends InputStreamFactory implements DataSource {
    	
    	private Clob clob;
    	private Charset charset = Charset.forName(Streamable.ENCODING);
    	
    	public ClobInputStreamFactory(Clob clob) {
    		this.clob = clob;
    	}
    	
    	public Charset getCharset() {
			return charset;
		}
    	
    	public void setCharset(Charset charset) {
			this.charset = charset;
		}
    	
    	@Override
    	public InputStream getInputStream() throws IOException {
    		try {
				return new ReaderInputStream(clob.getCharacterStream(), charset);
			} catch (SQLException e) {
				throw new IOException(e);
			}
    	}
    	
    	@Override
    	public Reader getCharacterStream() throws IOException {
    		try {
				return clob.getCharacterStream();
			} catch (SQLException e) {
				throw new IOException(e);
			}
    	}

		@Override
		public String getContentType() {
			return "text/plain"; //$NON-NLS-1$
		}

		@Override
		public String getName() {
			return "clob"; //$NON-NLS-1$
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public StorageMode getStorageMode() {
			return getStorageMode(clob);
		}
    	
    }
    
    public static class BlobInputStreamFactory extends InputStreamFactory implements DataSource {
    	
    	private Blob blob;
    	
    	public BlobInputStreamFactory(Blob blob) {
    		this.blob = blob;
    	}
    	
    	@Override
    	public InputStream getInputStream() throws IOException {
    		try {
				return blob.getBinaryStream();
			} catch (SQLException e) {
				throw new IOException(e);
			}
    	}
    	
    	@Override
    	public long getLength() {
    		if (length == -1) {
	    		try {
					length = blob.length();
				} catch (SQLException e) {
				}
    		}
    		return length;
    	}
    	
    	@Override
		public String getContentType() {
			return "application/octet-stream"; //$NON-NLS-1$
		}

		@Override
		public String getName() {
			return "blob"; //$NON-NLS-1$
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public StorageMode getStorageMode() {
			return getStorageMode(blob);
		}
    	
    }
    
    public static StorageMode getStorageMode(Object lob) {
    	if (lob instanceof Streamable<?>) {
    		return getStorageMode(((Streamable<?>)lob).getReference());
    	}
    	if (lob instanceof SerialClob) {
    		return StorageMode.MEMORY;
    	}
    	if (lob instanceof SerialBlob) {
    		return StorageMode.MEMORY;
    	}
		if (lob instanceof BaseLob) {
			BaseLob baseLob = (BaseLob)lob;
			try {
				return baseLob.getStreamFactory().getStorageMode();
			} catch (SQLException e) {
				return StorageMode.FREE;
			}
		}
		return StorageMode.OTHER;
    }
    
    public static class SQLXMLInputStreamFactory extends InputStreamFactory implements DataSource {
    	
    	protected SQLXML sqlxml;
    	
    	public SQLXMLInputStreamFactory(SQLXML sqlxml) {
    		this.sqlxml = sqlxml;
    	}
    	
    	@Override
    	public InputStream getInputStream() throws IOException {
    		try {
				return sqlxml.getBinaryStream();
			} catch (SQLException e) {
				throw new IOException(e);
			}
    	}
    	
    	@Override
    	public Reader getCharacterStream() throws IOException {
    		try {
				return sqlxml.getCharacterStream();
			} catch (SQLException e) {
				throw new IOException(e);
			}
    	}
    	
    	@Override
		public String getContentType() {
			return "application/xml"; //$NON-NLS-1$
		}

		@Override
		public String getName() {
			return "sqlxml"; //$NON-NLS-1$
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public StorageMode getStorageMode() {
			return getStorageMode(sqlxml);
		}
    	
    }
    
}
