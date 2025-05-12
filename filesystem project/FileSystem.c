#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdint.h>
#include <inttypes.h>
//these are the system includes



#define ATTR_LONG_NAME 0x00F //place holder for LDIR_Attr




typedef struct __attribute__((__packed__)) {
uint8_t BS_jmpBoot[ 3 ]; // x86 jump instr. to boot code
uint8_t BS_OEMName[ 8 ]; // What created the filesystem
uint16_t BPB_BytsPerSec; // Bytes per Sector
uint8_t BPB_SecPerClus; // Sectors per Cluster
uint16_t BPB_RsvdSecCnt; // Reserved Sector Count
uint8_t BPB_NumFATs; // Number of copies of FAT
uint16_t BPB_RootEntCnt; // FAT12/FAT16: size of root DIR
uint16_t BPB_TotSec16; // Sectors, may be 0, see below
uint8_t BPB_Media; // Media type, e.g. fixed
uint16_t BPB_FATSz16; // Sectors in FAT (FAT12 or FAT16)
uint16_t BPB_SecPerTrk; // Sectors per Track
uint16_t BPB_NumHeads; // Number of heads in disk
uint32_t BPB_HiddSec; // Hidden Sector count
uint32_t BPB_TotSec32; // Sectors if BPB_TotSec16 == 0
uint8_t BS_DrvNum; // 0 = floppy, 0x80 = hard disk
uint8_t BS_Reserved1; //
uint8_t BS_BootSig; // Should = 0x29
uint32_t BS_VolID; // 'Unique' ID for volume
uint8_t BS_VolLab[ 11 ]; // Non zero terminated string
uint8_t BS_FilSysType[ 8 ]; // e.g. 'FAT16 ' (Not 0 term.)
} BootSector;


typedef struct __attribute__((__packed__)) {
   char DIR_Name[11]; // Non zero terminated string
   uint8_t DIR_Attr; //File attributes
   uint8_t DIR_NTRes; //Used by Windows NT, ignore
   uint8_t DIR_CrtTimeTenth; //Tenths of sec. 0...199
   uint16_t DIR_CrtTime; //creation time in 2s intervals
   uint16_t DIR_CrtDate; //date file created
   uint16_t DIR_LstAccDate; //date of last read or write
   uint16_t DIR_FstClusHI; //top 16 bits file's 1st cluster
   uint16_t DIR_WrtTime; //time of last write
   uint16_t DIR_WrtDate; //date of last write
   uint16_t DIR_FstClusLO; //lower 16 bits file's 1st cluster
   uint32_t DIR_FileSize; //file size in bytes
} DirEntry;


typedef struct __attribute__((__packed__)) {
   uint8_t LDIR_Ord; // Order/ position in sequence/ set
   uint8_t LDIR_Name1[ 10 ]; // First 5 UNICODE characters
   uint8_t LDIR_Attr; // = ATTR_LONG_NAME (xx001111)
   uint8_t LDIR_Type; // Should = 0
   uint8_t LDIR_Chksum; // Checksum of short name
   uint8_t LDIR_Name2[ 12 ]; // Middle 6 UNICODE characters
   uint16_t LDIR_FstClusLO; // MUST be zero
   uint8_t LDIR_Name3[ 4 ]; // Last 2 UNICODE characters
} LongDirEntry;



//this struct represents a file in the FAT file system
typedef struct {
   FILE* file;
   uint16_t* fat;
   BootSector* bootSector;
   DirEntry* dirEntry;
   uint16_t currentCluster;
   off_t currentPosition;
} File;

//this decodes the long file name entry into a string
void decodeLongFileName(LongDirEntry* longDirEntry, wchar_t* decodedName) {
   wchar_t tempName[14];  // 5 + 6 + 2 (for null termination)
   memset(tempName, 0, sizeof(tempName));


   // Copy the UNICODE characters from each part of the long name
   memcpy(tempName, longDirEntry->LDIR_Name3, sizeof(longDirEntry->LDIR_Name3));
   memcpy(tempName + 2, longDirEntry->LDIR_Name2, sizeof(longDirEntry->LDIR_Name2));
   memcpy(tempName + 8, longDirEntry->LDIR_Name1, sizeof(longDirEntry->LDIR_Name1));


   // Append the decoded name to the provided buffer
   wcscpy(decodedName, tempName);
}

//function used to open file in the FAT volume
File* openFile(const char* filename, BootSector* bootSector, DirEntry* dirEntry, uint16_t* fat) {
   // Allocate memory for the File struct
   File* file = malloc(sizeof(File));
   if (file == NULL) {
       fputs("Memory error", stderr);
       exit(1);
   }

    // opens the file in binary "rb" which is read mode
   file->file = fopen(filename, "rb");
   if (file->file == NULL) {
       perror("Error opening file");
       free(file);
       return NULL;
   }

    // Set Fat, BootSector, and DirEntry for file struct
   file->fat = fat;
   file->bootSector = bootSector;
   file->dirEntry = dirEntry;
   //calculate the initial cluster number from high and low cluster values in dirEntry
   file->currentCluster = (dirEntry->DIR_FstClusHI << 16) | dirEntry->DIR_FstClusLO; 
   file->currentPosition = 0;


   return file;
}



//function to seek to specified position in file
off_t seekFile(File* file, off_t offset, int whence) { 
   off_t newPosition;

    //choose where the new position will be based on whence
   switch (whence) {
       case SEEK_SET:
       //sets the new position to the offset value
           newPosition = offset;
           break;
       case SEEK_CUR:
       //sets new position to current position added to the offset value given
           newPosition = file->currentPosition + offset;
           break;
       case SEEK_END:
       //seeks from end of the file
           newPosition = file->dirEntry->DIR_FileSize + offset;
           break;
       default:
           return -1; // Unsupported whence value
   }

    //ensures the new position is within valid range
   newPosition = (newPosition < 0) ? 0 : newPosition; //if position is negative, set to 0
   // ensures the new position is within the filesize, if its greater set to max
   newPosition = (newPosition > file->dirEntry->DIR_FileSize) ? file->dirEntry->DIR_FileSize : newPosition;

    //updates the current position field in the file struct defined earlier
   file->currentPosition = newPosition;


   // Update currentCluster based on newPosition
   file->currentCluster = (newPosition / file->bootSector->BPB_BytsPerSec) / file->bootSector->BPB_SecPerClus;

    //returns the newPosition variable
   return newPosition;
}





/**
 * @brief reads data from the file given in int main
 *
 * @param file      pointer to file structure which is the open file MyFile in int main
 * @param buffer    pointer to buffer where read data is stored
 * @param length    number of bytes set to be read
 * @return size_t   number of bytes read
 */
size_t readFile(File* file, void* buffer, size_t length) {
   size_t bytesRead = 0; //initialises the total number of bytes that have been read
   size_t bytesToRead = length; //sets the bytes to read to the length specified by user in int main
   size_t clusterSize = file->bootSector->BPB_BytsPerSec * file->bootSector->BPB_SecPerClus; // calculates the size of file cluster

    //reads until all requised bytes are read or EOF
   while (bytesToRead > 0 && file->currentPosition < file->dirEntry->DIR_FileSize) {
       size_t sectorOffset = (file->currentPosition % clusterSize) / file->bootSector->BPB_BytsPerSec; //finds the setor offscet within the current cluster
       size_t byteOffset = file->currentPosition % file->bootSector->BPB_BytsPerSec; //finds the byte offset within sector
       size_t sectorNumber = (file->currentCluster - 2) * file->bootSector->BPB_SecPerClus + sectorOffset; //calculates the sector number

        //sets file pointer to speified pcosition
       fseek(file->file, sectorNumber * file->bootSector->BPB_BytsPerSec + byteOffset, SEEK_SET);


       size_t bytesRemainingInSector = file->bootSector->BPB_BytsPerSec - byteOffset; //finds remaining bytes in current sector
       size_t readSize = (bytesRemainingInSector < bytesToRead) ? bytesRemainingInSector : bytesToRead; //determines the size to read for current iteration

        //reads data from file and puts it into the buffer
       fread(buffer + bytesRead, 1, readSize, file->file);


       bytesRead += readSize; //updates the byte number read
       bytesToRead -= readSize; // updates the remaining bytes to read
       file->currentPosition += readSize; //updates current file pos

        //moves to next cluster if current has been read fully
       if (byteOffset + readSize == file->bootSector->BPB_BytsPerSec) {
           file->currentCluster = file->fat[file->currentCluster]; // move to next cluster using FAT the file allocation table
       }
   }


   return bytesRead; //returns number of bytes successfully read
}



//closes file asociated with given file structure
void closeFile(File* file) {
    //file stream closed
   fclose(file->file);
   //frees allocated memory for file structure
   free(file);
}







    //decodes date and time from 
void decodeDateTime(uint16_t wrtTime, uint16_t wrtDate, int *year, int *month, int *day, int *hour, int *minute, int *second) {
   *second = (wrtTime & 0x1F) * 2; //gets seconds by masking the lowest 5 bits of wrtTime and times by 2 
   *minute = (wrtTime >> 5) & 0x3F; //to get minutes, right shift 5 bits and masks next 6 bits
   *hour = (wrtTime >> 11) & 0x1F; //to get hours, right shift 11 bits and masks 5 next bits


   *day = wrtDate & 0x1F; //extracts day, lowest 5 bits of wrtDate
   *month = (wrtDate >> 5) & 0x0F; // gets month by right shift 5 and masking next 4 bits 
   *year = ((wrtDate >> 9) & 0x7F) + 1980; //right shift 9 bits and masks the next 7 bits 
    //we add 1980 as in FAT the year values start from year 1980
}


void listFilesInRoot(FILE* file, BootSector* bootSector) {
   // Calculate the starting sector and size of the root directory

   //adds reserved sectors, FAT size and product of the number of Fats and the size of each FAT
   long rootDirStartSector = bootSector->BPB_RsvdSecCnt + bootSector->BPB_NumFATs * bootSector->BPB_FATSz16;
   
   //Calculates the total size of root directory by multiplying the number of entries with the size of each entry
   long rootDirSize = bootSector->BPB_RootEntCnt * sizeof(DirEntry);


   // Allocate memory for the root directory
   DirEntry* rootDir = (DirEntry*)malloc(rootDirSize); //creates a pointer rootDir to manage that memory
   if (rootDir == NULL) {
       fputs("Memory error", stderr); //prints error message if memory allocation fails, stderr is a standard file stream used for outputting error messages
       exit(1); //exits program is memory allocation fails
   }


   // Read the root directory from the file
   fseek(file, rootDirStartSector * bootSector->BPB_BytsPerSec, SEEK_SET); //move file cursor to start of root directory
   fread(rootDir, 1, rootDirSize, file); //reads root directory into memory allocated


   // output header for the "table" we output
      printf("%-12s %-10s %-20s %-6s %-8s %s\n", "StartCluster  ", "LastModified      ", "Attributes    ", "Size", "     Filename", " ");
    //explanation of the format:
    // %-12s left-aligns and reserves 12 characters for the start cluster heading
    // %s is a string format for the space between columns and the newline

   // Iterate through each directory entry
    for (int i = 0; i < bootSector->BPB_RootEntCnt; ++i) {
       // Check if the entry is a long file name entry
       if (rootDir[i].DIR_Attr == ATTR_LONG_NAME) {
           // Decode and print the long file name
           LongDirEntry* longDirEntry = (LongDirEntry*)&rootDir[i];
           wchar_t decodedName[100];  // Maximum length for a long file name
           decodeLongFileName(longDirEntry, decodedName); //calls the decodelongfilename function with the parameters
           wprintf(L"%ls", decodedName); // wprintf prints formatted wide characters, the L is used to denote wide character
       } else if (rootDir[i].DIR_Name[0] != 0xE5 && rootDir[i].DIR_Name[0] != 0x00) { //assignment told us to ignore entries with first byte of 0 and 0xE5
           // Extract information from the directory entry
           uint16_t startCluster = (rootDir[i].DIR_FstClusHI << 16) | rootDir[i].DIR_FstClusLO; //combines the high and low parts of the startCluster


           // Decode last modified time and date
           int year, month, day, hour, minute, second;
           decodeDateTime(rootDir[i].DIR_WrtTime, rootDir[i].DIR_WrtDate, &year, &month, &day, &hour, &minute, &second);
        
           // Convert attributes to a string
           char attributes[7];
           //uses the DIR_Attr values which describes the properties, the 0x01, 0x02 etc.. are hex values corresponding to the masks to extract bits
           attributes[0] = (rootDir[i].DIR_Attr & 0x01) ? 'R' : '-'; // read-only
           attributes[1] = (rootDir[i].DIR_Attr & 0x02) ? 'H' : '-'; // hidden
           attributes[2] = (rootDir[i].DIR_Attr & 0x04) ? 'S' : '-'; // system
           attributes[3] = (rootDir[i].DIR_Attr & 0x08) ? 'V' : '-'; // volumeID
           attributes[4] = (rootDir[i].DIR_Attr & 0x10) ? 'A' : '-'; // archive
           attributes[5] = (rootDir[i].DIR_Attr & 0x20) ? 'D' : '-'; // directory
           attributes[6] = '\0'; //null termination


            printf("%-12u %04d-%02d-%02d %02d:%02d:%02d %-20s %-6u %-8s %s\n",
            // %-12u prints the start cluster of the file in a 12 character wide field
            // %04d %02d %02d prints the date in format YYYY-MM-DD
                  startCluster,
                  year, month, day, hour, minute, second,
                  attributes,
                  rootDir[i].DIR_FileSize,
                  rootDir[i].DIR_Name);
       }
   }


   // Free allocated memory for the root directory
   free(rootDir);
}












void loadFAT(FILE* file, BootSector* bootSector, uint16_t** fat) {
   // Calculate the starting sector and size of the FAT
   long fatStartSector = bootSector->BPB_RsvdSecCnt; //caulcates starting sector of the FAT
   long fatSize = bootSector->BPB_FATSz16 * bootSector->BPB_BytsPerSec; //calculates the size of the FAT in bytes




   //allocate memory for the fat
   *fat = (uint16_t*)malloc(fatSize); //*fat is a pointer to an array of uint16_t that stores the FAT data
   if (*fat == NULL) {
       fputs("memory error", stderr);
       exit(1);
   }


   fseek(file, fatStartSector * bootSector->BPB_BytsPerSec, SEEK_SET); //moves file pointer to beginning of the FAT in the file
   fread(*fat, 1, fatSize, file); //reads the FAT data from the file into the allocated memory
}


void listFileClusters(uint16_t* fat, uint16_t startCluster) {
   printf("Cluster List for File starting at Cluster %u:\n", startCluster);


   while (1) { //loops until condition met
       printf("%u -> ", startCluster); //prints the current cluster number then adds an arrow


       // Break the loop if an invalid cluster value is encountered
       if (startCluster >= 0xfff8) { //if startcluster is invalid, breaks out the loop, 0xfff8 is a way to mark end of the cluster chain
           break;
       }


       startCluster = fat[startCluster]; //updates startCluster with next clustser within FAT
   }


   printf("%u\n", startCluster); //prints the last cluster in the chain then ends the loop
}








int main() {
    //variables used for file handling
   long lSize;
   char *buffer;
   size_t result;
   long offset; //update this to choose where to start from
   int whence; //position from where offset is added
   DirEntry dirEntry;
   BootSector bootSector;


   // Seek to a specific position (replace 0, SEEK_SET with your desired offset and whence)

    //opens the fat16 file
   FILE* file;
   file = fopen("fat16.img", "rb");


   if (file == NULL) {
       printf("could not open the file\n");
       return 0;
   }




   whence = 5000; //this will be the end number of bytes read
   offset = 1000; //this will be the position from where the file starts reading the file from
   //gets filesize then rewinds the file back to the start so the pointer isnt at the end
   fseek(file, 0, SEEK_END);
   lSize = ftell(file);
   rewind(file);

   //allocates memory for the file content
   buffer = (char*)malloc(sizeof(char) * lSize); //dynamically allocates mmemory for the buffer to store file content, uses malloc to allocate block of memory
   if (buffer == NULL) {
       fputs("Memory error", stderr);
       exit(1);
   }

   //reads the file content into the buffer
   result = fread(buffer, 1, lSize, file); 
   if (result != lSize) {
       fputs("Reading error", stderr);
       exit(3);
   }


   memcpy(&bootSector, buffer, sizeof(BootSector)); //uses memcpy to copy data from loaded file buffer into bootsector and direntry
   memcpy(&dirEntry, buffer + sizeof(BootSector), sizeof(DirEntry));
 

    //outputs information about the boot sector structure, good to ensure it has been loaded from the file properly
   printf("Bytes per Sector: %u\n", bootSector.BPB_BytsPerSec);
   printf("Sectors per Cluster %u\n", bootSector.BPB_SecPerClus);
   printf("Reserved Sector Count %u\n", bootSector.BPB_RsvdSecCnt);
   printf("Number of copies of FAT: %u\n", bootSector.BPB_NumFATs);
   printf("FAT12/FAT16: size of root DIR: %u\n", bootSector.BPB_RootEntCnt);
   printf("Sectors, may be 0: %u\n", bootSector.BPB_TotSec16);
   printf("Sectors in FAT (FAT12 or FAT16): %u\n", bootSector.BPB_FATSz16);
   printf("Sectors if BPB_TotSec16 == 0: %u\n", bootSector.BPB_TotSec32);
   printf("Non zero terminated string: %s\n", bootSector.BS_VolLab);
   printf("%u\n", dirEntry.DIR_FileSize); //determines total file size
   uint16_t* fat;
   loadFAT(file, &bootSector, &fat); //declares a pointer fat to store the dynamically allocated memory for the FAT, this then loads the FAT from file into memory
   uint16_t startCluster = 5; //change this to change starting cluster number


   listFileClusters(fat, startCluster); //calls function 
   free(fat); //frres the memory allocated for FAT
   listFilesInRoot(file, &bootSector); //calls function



  


   File* myFile = openFile("Fat16.img", &bootSector, &dirEntry, fat); //opens file using openfile function
   if (file == NULL) {
       perror("Error opening file"); //error handling
       free(file);
       return NULL;
   }
   if (myFile != NULL) {
       off_t byteNumber = (2456 - 2) * bootSector.BPB_SecPerClus * bootSector.BPB_BytsPerSec; //calculates the number of bytes where the starting sector is
       //need to -2 as first 2 clusters are needed for bootstrapping
       off_t newPosition = seekFile(myFile, byteNumber, SEEK_CUR); //calls seekFile, change 2456 to choose cluster you start at



        if (newPosition != -1) {
           // Read and output the content
           char *buffer = malloc((size_t)dirEntry.DIR_FileSize); //dynamically allocates a block of memor ylarge enough to hold the entire file
            if (buffer == NULL) {
               fputs("Memory error", stderr); //prints error if buffer == null
               exit(1);
           }
           size_t BytesToRead = 1000;
           size_t bytesRead = readFile(myFile, buffer, BytesToRead); //calls readfile function and stores output in this variable

            if (bytesRead > 0) { //condition ensures the bytes are read correctly
               // Output the contents
               fwrite(buffer, 1, bytesRead, stdout); // writes contents of the buffer to output, 1 is the size of each character
               printf("\n");
           
           
            
           
           
           
           } else {
               perror("Error reading file"); //error handling
           }
       } else {
           perror("Error seeking file"); //error handling
       }
        closeFile(myFile); //closes the file using the closeFile function
   }






   free(buffer); //frees the rest of the memory
   return 0;
   }

