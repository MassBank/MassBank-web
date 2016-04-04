#!/usr/bin/python

from Crypto.Cipher import Blowfish
from Crypto import Random
from struct import pack
from binascii import hexlify, unhexlify


IV = "<IVcleartext>"
KEY = "<PASScleartext>"

# We'll use the Base64 string from JBlowfish output
ciphertext = "<base64encodedcryptedstuff>".decode("base64")

cipher = Blowfish.new(KEY, Blowfish.MODE_CBC, IV)
message = cipher.decrypt(ciphertext)
print("KEY: " + KEY.encode("hex"))
print("IV: " + IV.encode("hex"))
print("Message: " + message)

