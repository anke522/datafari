/*******************************************************************************
 /*******************************************************************************
 *  * Copyright 2015 France Labs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *******************************************************************************/
package com.francelabs.datafari.updateprocessor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

public class DatafariUpdateProcessor extends UpdateRequestProcessor {

  public DatafariUpdateProcessor(final SolrParams params, final UpdateRequestProcessor next) {
    super(next);
  }

  @Override
  public void processAdd(final AddUpdateCommand cmd) throws IOException {
    final SolrInputDocument doc = cmd.getSolrInputDocument();

    // Sometimes Tika put several ids so we keep the first one which is
    // always the right one
    if (doc.getFieldValues("id").size() > 1) {
      final Object id = doc.getFieldValue("id");
      doc.remove("id");
      doc.addField("id", id);
    }

    // Try to retrieve at the ignored_filelastmodified field to set it's
    // value in the last_modified field
    if (doc.getFieldValue("ignored_filelastmodified") != null) {
      final Object last_modified = doc.getFieldValue("ignored_filelastmodified");
      doc.remove("last_modified");
      doc.addField("last_modified", last_modified);
    }

    // Sometimes Tika put several last_modified dates, so we keep the first
    // one which is always the right one
    if (doc.getFieldValues("last_modified") != null && doc.getFieldValues("last_modified").size() > 1) {
      final Object last_modified = doc.getFieldValue("last_modified");
      doc.remove("last_modified");
      doc.addField("last_modified", last_modified);
    }

    final String url = (String) doc.getFieldValue("id");

    // Create path hierarchy for facet
    final List<String> urlHierarchy = new ArrayList<>();

    /*
     * // Create path hierarchy for facet
     *
     * final List<String> urlHierarchy = new ArrayList<String>();
     *
     * final String path = url.replace("file:", ""); int previousIndex = 1; int
     * depth = 0; // Tokenize the path and add the depth as first character for
     * each token // (like: 0/home, 1/home/project ...) for (int i = 0; i <
     * path.split("/").length - 2; i++) { int endIndex = path.indexOf('/',
     * previousIndex); if (endIndex == -1) { endIndex = path.length() - 1; }
     * urlHierarchy.add(depth + path.substring(0, endIndex)); depth++;
     * previousIndex = endIndex + 1; }
     *
     * // Add the tokens to the urlHierarchy field doc.addField("urlHierarchy",
     * urlHierarchy);
     */

    doc.remove("url");
    doc.addField("url", url);

    String filename = "";
    String jsouptitle = "";
    if (doc.getField("jsoup_title") != null)
      jsouptitle = (String) doc.getFieldValue("jsoup_title");

    final SolrInputField streamNameField = doc.get("ignored_stream_name");
    if (streamNameField != null) {
      filename = (String) streamNameField.getFirstValue();
    } else {
      final Pattern pattern = Pattern.compile("[^/]*$");
      final Matcher matcher = pattern.matcher(url);
      if (matcher.find()) {
        filename = matcher.group();
      }
    }

    if (url.startsWith("http")) {
      if (doc.get("title") == null) {
        if (doc.get("jsoup_title") != null) {
          doc.removeField("title");
          doc.addField("title", jsouptitle);
        } else {
          doc.addField("title", filename);
        }
      }
      doc.addField("source", "web");
    }

    if (url.startsWith("file")) {
      // keep the filename as the first title for the searchView of Datafari
      if (doc.getFieldValues("title") != null) {
        final Collection<Object> titleValues = doc.getFieldValues("title");
        doc.removeField("title");
        doc.addField("title", filename);
        for (final Object value : titleValues) {
          doc.addField("title", value);
        }
      } else {
        doc.addField("title", filename);
      }

      doc.addField("source", "file");
    }

    // Ensure a search-able title
    String language = (String) doc.getFieldValue("language");
    if (language == null) {
      language = "en";
    }
    doc.addField("title_" + language, filename);
    doc.addField("exactTitle", filename);
    if (!jsouptitle.isEmpty()) {
      doc.addField("title_" + language, jsouptitle);
      doc.addField("exactTitle", jsouptitle);
    }

    String extension = "";
    String mime = "";
    final URL urlObject = new URL(url);
    final String path = urlObject.getPath();
    final SolrInputField mimeTypeField = doc.get("ignored_content_type");

    final String nameExtension = FilenameUtils.getExtension(path);
    final String tikaExtension = mimeTypeField == null ? "" : extensionFromMimeTypeField(mimeTypeField);

    extension = nameExtension.length() > 1 && nameExtension.length() < 5 ? nameExtension : tikaExtension;
    mime = tikaExtension.length() > 1 && tikaExtension.length() < 5 ? tikaExtension : nameExtension;
    /*
     * if (extensionFromName || mimeTypeField == null) { if
     * (path.contains(".")){ extension = FilenameUtils.getExtension(path); if
     * (extension.length() > 4 || extension.length() < 1) { // If length is too
     * long, try extracting from tika information if available String
     * tryExtension = mimeTypeField==null ? null :
     * extensionFromMimeTypeField(mimeTypeField); if (tryExtension != null) {
     * extension = tryExtension; } else { // Else default to bin for anything
     * else extension = "bin"; } } } else if
     * (urlObject.getProtocol().equals("http") ||
     * urlObject.getProtocol().equals("https")) { extension = null; if
     * (mimeTypeField != null) { extension =
     * extensionFromMimeTypeField(mimeTypeField); } if (extension == null) {
     * extension = "html"; } } } else { extension =
     * extensionFromMimeTypeField(mimeTypeField); if (extension == null) {
     * extension = FilenameUtils.getExtension(path); } }
     */
    doc.addField("extension", extension.toLowerCase());
    doc.addField("mime", mime.toLowerCase());

    super.processAdd(cmd);
  }

  private String extensionFromMimeTypeField(final SolrInputField mimeTypeField) {
    String extension = "";
    final String[] mimeTypeList = mimeTypeField.getValues().toArray(new String[0]);
    for (String mimeType : mimeTypeList) {
      if (mimeType.contains(";")) {
        final String[] parts = mimeType.split(";");
        mimeType = parts[0];
      }
      final MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
      MimeType type;

      try {
        type = allTypes.forName(mimeType);
        String currentExtension = type.getExtension();
        if (currentExtension.length() > 1) {
          currentExtension = currentExtension.substring(1);
        }
        if (!currentExtension.equals("bin")) {
          extension = currentExtension;
        }
      } catch (final MimeTypeException e) {
      }
    }
    return extension;
  }
}
