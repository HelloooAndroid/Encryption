# Encryption 
encryption is the process of encoding information. This process converts the original representation of the information, known as plaintext, into an alternative form known as ciphertext. Only authorized parties can decipher a ciphertext back to plaintext and access the original information.

You can store data in Databse or Preferences after encrypting them.
But what about the keys used to encrypt the data? A general rule is you should not use any hardcoded keys because a hacker can easily decompile your code and obtain the key, thereby rendering the encryption useless. You need a key management framework, and that’s what the Android KeyStore API is designed for.

KeyStore provides two functions:
1) Randomly generates keys and
2) Securely stores the keys

With these, storing secrets becomes easy. All you have to do is:
- Generate a random key when the app runs the first time;
- When you want to store a secret, retrieve the key from KeyStore, encrypt the data with it, and then store the encrypted data in Preferences.
- When you want to read a secret, read the encrypted data from Preferences, get the key from KeyStore and then use the key to decrypt the data.

Your key is randomly generated and securely managed by KeyStore and only your code can read it.

### Followings are the options to generate and store keys safely depending on Android API level.
## <b>API Level < 18:</b> 
Android Keystore not present. Request a password to the user, derive an encryption key from the password, The drawback is that you need to prompt for the password when application starts. The encryption key it is not stored in the device. It is calculated each time when the application is started using the password

## <b>API Level >=18 <23:</b> 
Android Keystore available without AES support. Generate a random AES key using the default cryptographic provider (not using AndroidKeystore). 
### Key Generation
- Generate a pair of RSA keys
- Generate a random AES key
- Encrypt the AES key using the RSA public key
- Store the encrypted AES key in Preferences
### Encrypting and Storing the data
- Retrieve the encrypted AES key from Preferences
- Decrypt the above to obtain the AES key using the private RSA key
- Encrypt the data using the AES key
### Retrieving and decrypting the data
- Retrieve the encrypted AES key from Preferences
- Decrypt the above to obtain the AES key using the private RSA key
- Decrypt the data using the AES key  

<br />

<b>Why we haven't use only RSA for encryption?  
RSA cannot encrypt data larger than it’s key size (padding affects this as well). If data size is unpredictable then use AES. Some possible solutions include separating the data into chunks and running the cipher encryption on each section, Though note that this will make the encryption very slower. We encrypt data using AES and key wrap the AES key using a RSA key backed by the keystore.</b>
<br />
<br />


## <b>API Level >=23:</b> 
Android Keystore available with AES support. Generate a random AES key using into Android Keystore. You can use it directly.
#### Generating the key
#### Getting the key
#### Encrypting the data
#### Decrypting the data

