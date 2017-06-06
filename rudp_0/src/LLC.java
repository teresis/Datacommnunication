import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class LLC {
	private byte[] DAddr= new byte[6];
	private byte[] SAddr= new byte[6];
	private byte[] LPDU = new byte[2];
	private byte[] SAP = new byte[2];
	private byte[] CONTROL=new byte[2];

	private int length;
	public void setDaddr(byte[] daddr){
		DAddr= daddr;
	}
	public byte[] getDaddr()
	{
		return DAddr;
	}
	public void setSaddr(byte[] saddr){
		SAddr= saddr;
	}	
	public byte[] getSaddr()
	{
		return SAddr;
	}
	public byte[] getLPDU()
	{
		return LPDU;
	}
	public void setLPDU(){
	
			length = 20+CONTROL.length;
		
		LPDU = intTo2ByteArray(length);
	}
	public byte[] getSAP()
	{
		return SAP;
	}
	public void setCONTROL(byte type1, byte type2, int command){
		CONTROL= new byte[2];
		if((type1&0XC0)==0x80){//Sformat
			CONTROL[1]=type2;
			if(command==1){//RR
				CONTROL[0]=(byte) 0x80;	
			}
			else{//NAK
				CONTROL[0]=(byte) 0x90;
			}
		}
		else if((type1 & 0xC0)==0XC0){//Uformat
			CONTROL =new byte[1];
			if(command ==0)
			CONTROL[0] =(byte) 0xF6;//SABME
			else
			CONTROL[0] =(byte)0xC6;//UA
		}
		else{//Iformat
			CONTROL[0]=type1;
			CONTROL[1]=type2;
		}
	}
	public byte[] getCONTROL()
	{
		return CONTROL;
	}
	public void setINFORM(byte[] INPUT) throws IOException
	{
		ByteArrayOutputStream ops = new ByteArrayOutputStream();
		ops.write(CONTROL);
		ops.write(INPUT);
		CONTROL = ops.toByteArray();
	}
	public static byte[] intTo2ByteArray(int a)
	{
		byte[] arr = new byte[2];
		arr[1] = (byte) (a & 0xFF);
		arr[0] = (byte) ((a >> 8) & 0xFF);
		return arr; 
	}
	public void setLLC(byte[] buf ,int command)
	{
		System.arraycopy(buf, 0, SAddr, 0, 6);
		System.arraycopy(buf, 6, DAddr, 0, 6);
		System.arraycopy(buf, 12, LPDU, 0, 2);
		ByteBuffer buf1 = ByteBuffer.wrap(LPDU);
		length= buf1.getShort();
		if(command != 0){//if Iformat
		
		
		}
	}
	public byte[] getLLC() throws IOException
	{
		CRC32 checksum = new CRC32();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(this.getDaddr());
		outputStream.write(this.getSaddr());
		outputStream.write(this.getLPDU());
		outputStream.write(this.getSAP());
		outputStream.write(this.getCONTROL());
		byte[] output=outputStream.toByteArray();
		checksum.update(output);
		int check = (int)checksum.getValue();
		//System.out.println(check);
		byte[] crc= intTo4ByteArray(check);
		outputStream.write(crc);
		output =outputStream.toByteArray();
		return output;
	}
	public static byte[] intTo4ByteArray(int a) {
        byte[] arr = new byte[4];
        arr[3] = (byte) (a & 0xFF);
        arr[2] = (byte) ((a >> 8) & 0xFF);
        arr[1] = (byte) ((a >> 16) & 0xFF);
        arr[0] = (byte) ((a >> 24) & 0xFF);
        return arr;
    }
}
