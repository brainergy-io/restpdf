//package io.brainergy.pdf.comparator;
//
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.signatures.SignatureUtil;
//
//import java.io.IOException;
//import java.util.List;
//
///**
// * This is a simple tool to compare revisions (signed and full file only)
// * of a PDF.
// *
// * @author mkl
// */
//public class PdfRevisionCompare extends PdfCompare {
//
//    public PdfRevisionCompare(PdfDocument pdfDocument1, PdfDocument pdfDocument2) {
//        super(pdfDocument1, pdfDocument2);
//    }
//
//    public static boolean hasChange(String arg) throws IOException {
//        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(arg))) {
//            SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
//            List<String> signatureNames = signatureUtil.getSignatureNames();
//
//            String previousRevision = signatureNames.get(0);
//            PdfDocument previousDocument = new PdfDocument(new PdfReader(signatureUtil.extractRevision(previousRevision)));
//            new SignatureUtil(previousDocument).getSignatureNames(); // to mark signature values as unencrypted
//
//            for (int i = 1; i < signatureNames.size(); i++) {
//                String currentRevision = signatureNames.get(i);
//                PdfDocument currentDocument = new PdfDocument(new PdfReader(signatureUtil.extractRevision(currentRevision)));
////                new SignatureUtil(currentDocument).getSignatureNames(); // to mark signature values as unencrypted
//
//                previousDocument.close();
//                previousDocument = currentDocument;
//                previousRevision = currentRevision;
//            }
//
//            previousDocument.close();
//            return signatureUtil.signatureCoversWholeDocument(previousRevision);
//        }
//    }
//
//}