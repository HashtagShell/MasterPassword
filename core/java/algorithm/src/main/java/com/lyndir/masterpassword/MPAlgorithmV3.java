//==============================================================================
// This file is part of Master Password.
// Copyright (c) 2011-2017, Maarten Billemont.
//
// Master Password is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Master Password is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You can find a copy of the GNU General Public License in the
// LICENSE file.  Alternatively, see <http://www.gnu.org/licenses/>.
//==============================================================================

package com.lyndir.masterpassword;

import static com.lyndir.masterpassword.MPUtils.idForBytes;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import com.lyndir.lhunath.opal.system.CodeUtils;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;


/**
 * @see MPMasterKey.Version#V3
 *
 * @author lhunath, 2014-08-30
 */
public class MPAlgorithmV3 extends MPAlgorithmV2 {

    @Override
    public MPMasterKey.Version getAlgorithmVersion() {

        return MPMasterKey.Version.V3;
    }

    @Override
    public byte[] deriveKey(final String fullName, final char[] masterPassword) {
        Preconditions.checkArgument( masterPassword.length > 0 );

        byte[] fullNameBytes = fullName.getBytes( MPAlgorithmV0.mpw_charset );
        byte[] fullNameLengthBytes = MPUtils.bytesForInt( fullNameBytes.length );
        ByteBuffer mpBytesBuf = MPAlgorithmV0.mpw_charset.encode( CharBuffer.wrap( masterPassword ) );

        logger.trc( "-- mpw_masterKey (algorithm: %u)", getAlgorithmVersion().toInt() );
        logger.trc( "fullName: %s", fullName );
        logger.trc( "masterPassword.id: %s", (Object) idForBytes( mpBytesBuf.array() ) );

        String keyScope = MPKeyPurpose.Authentication.getScope();
        logger.trc( "keyScope: %s", keyScope );

        // Calculate the master key salt.
        logger.trc( "masterKeySalt: keyScope=%s | #fullName=%s | fullName=%s",
                    keyScope, CodeUtils.encodeHex( fullNameLengthBytes ), fullName );
        byte[] masterKeySalt = Bytes.concat( keyScope.getBytes( MPAlgorithmV0.mpw_charset ), fullNameLengthBytes, fullNameBytes );
        logger.trc( "  => masterKeySalt.id: %s", CodeUtils.encodeHex( idForBytes( masterKeySalt ) ) );

        // Calculate the master key.
        logger.trc( "masterKey: scrypt( masterPassword, masterKeySalt, N=%lu, r=%u, p=%u )",
                    MPAlgorithmV0.scrypt_N, MPAlgorithmV0.scrypt_r, MPAlgorithmV0.scrypt_p );
        byte[] mpBytes = new byte[mpBytesBuf.remaining()];
        mpBytesBuf.get( mpBytes, 0, mpBytes.length );
        Arrays.fill( mpBytesBuf.array(), (byte) 0 );
        byte[] masterKey = scrypt( masterKeySalt, mpBytes ); // TODO: Why not mpBytesBuf.array()?
        Arrays.fill( masterKeySalt, (byte) 0 );
        Arrays.fill( mpBytes, (byte) 0 );
        logger.trc( "  => masterKey.id: %s", (Object) idForBytes( masterKey ) );

        return masterKey;
    }
}
