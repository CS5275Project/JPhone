package jPhone;

import java.util.concurrent.BlockingQueue;
import java.net.*;

import jlibrtp.*;

/**
 * this class sends out audio data to participants
 * @author terry modified by Nelson
 *
 */
public class RTPHandler implements RTPAppIntf {
	
	/**
	 * the RTP session
	 */
	private RTPSession m_rtpReceiveSession;
	
	private RTPSession m_rtpSendSession;
	
	private String m_recevierIPAddr;
	
	
	/**
	 * when an audio block is received, put the block into this queue.
	 * Note that for this queue, RTPReceiver is the producer, SoundPlayer is the consumer.
	 */
	private BlockingQueue<AudioBlock> m_carrierQueue;
	/**
	 * when an audio block is received, RTPReceiver first obtain a blank block from this queue and then write the block.
	 * Note that for this queue, RTPReceiver is the consumer, SoundPlayer is the producer.
	 */
	private BlockingQueue<AudioBlock> m_returnQueue;


	
	private boolean closed = false;
	

	/**
	 * the constructor
	 * @param receiverIPAddrs specify the participants' IP addresses
	 * @param RTPport specify the UDP port used for RTP communication
	 */
	public RTPHandler(String receiverIPAddr, int rtpReceiverPort, int rtcpReceiverPort, int rtpSenderPort, int rtcpSenderPort, BlockingQueue<AudioBlock> carrierQueue, BlockingQueue<AudioBlock> returnQueue)
	{
		
		m_recevierIPAddr = receiverIPAddr;
		m_carrierQueue = carrierQueue;
		m_returnQueue = returnQueue;
		
		// Receive Session
		System.out.println("Receive from " + m_recevierIPAddr + " at " + rtpReceiverPort + " " + rtcpReceiverPort);
		

		
		DatagramSocket rtpReceiverSocket = null;
		DatagramSocket rtcpReceiverSocket = null;
		
		try
		{
			rtpReceiverSocket = new DatagramSocket(rtpReceiverPort); 			
			rtcpReceiverSocket = new DatagramSocket(rtcpReceiverPort); 			
		}
		catch(Exception e)
		{
			System.err.println("RTP session failed to obtain ports.");
			e.printStackTrace();
			System.exit(1);
		}
		
		/*****************************************************************
		 * Task 2                                                        *
		 * TODO: 														 *
		 * Initialize a RTP receive session RTPSession;                  *
		 * Set the naivePktReception to be true;                         *
		 * Register the RTP session.                                     *
		 *****************************************************************/
/**** Nelson Code Add by Nelson** Start */		
		m_rtpReceiveSession = new RTPSession(rtpReceiverSocket, rtcpReceiverSocket);
		m_rtpReceiveSession.naivePktReception(true);
		m_rtpReceiveSession.RTPSessionRegister(this, null, null);
/**** Nelson Code Add by Nelson** End */			
		//Send Session 
		
		DatagramSocket rtpSenderSocket = null;
		DatagramSocket rtcpSenderSocket = null;
		
		try
		{
			rtpSenderSocket = new DatagramSocket(); 	
			rtcpSenderSocket = new DatagramSocket(); 	
		}
		catch(Exception e)
		{
			System.err.println("RTP session failed to obtain ports.");
			e.printStackTrace();
			System.exit(1);
		}
		
		/*****************************************************************
		 * Task 2                                                        *
		 * TODO: 														 *
		 * Initialize a RTP send session RTPSession;                     *
		 * Register the RTP session.                                     *
		 *****************************************************************/
/**** Nelson Code Add by Nelson***/	
			m_rtpSendSession = new RTPSession(rtpSenderSocket, rtcpSenderSocket);	
			m_rtpSendSession.RTPSessionRegister(this, null, null);
/**** Nelson Code Add by Nelson***/				
		System.out.println("Send to " + m_recevierIPAddr + " " + rtpSenderPort + " " + rtcpSenderPort);
		m_rtpSendSession.addParticipant(new Participant(m_recevierIPAddr, rtpSenderPort, rtcpSenderPort));
		
		
	}

	public int frameSize(int payloadType) {
		return 1;
	}

	public boolean isclosed(){
	 return closed;
	}
	
	public void receiveData(DataFrame frame, Participant participant) {
		byte[] data = frame.getConcatenatedData(); // obtain the audio bytes carried by RTP frame
		AudioBlock block = null;
		
		System.out.println("Data received ...");
		
		try
		{
			block = m_returnQueue.take(); // take a blank block from the return queue
			System.arraycopy(data, 0, block.data, 0, data.length); // copy the audio data
			block.nBytes = data.length;
			m_carrierQueue.put(block); // put the block into the carrier queue 
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void userEvent(int type, Participant[] participant) {
		// do nothing
	}

	/**
	 * send out data
	 * @param data the bytes to be sent
	 */
	public void sendData(byte data[])
	{
		
		System.out.println("Data sent ...");
		m_rtpSendSession.sendData(data);
	}

	public void close() {
		/*****************************************************************
		 * Task 3                                                        *
		 * TODO: 														 *
		 * End the RTP send session;                                     *
		 * End the RTP receive session.                                  *
		 *****************************************************************/
/**** Nelson Code Add by Nelson***/	
		m_rtpReceiveSession.endSession();
		m_rtpSendSession.endSession();
/**** Nelson Code Add by Nelson***/	
	}
}
